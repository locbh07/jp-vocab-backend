package com.jpvocab.vocabsite.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class JlptImportTool {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Charset[] ENCODINGS = new Charset[] {
            StandardCharsets.UTF_8,
            Charset.forName("Shift_JIS"),
            Charset.forName("CP932")
    };

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: JlptImportTool <json_dir> [--dry-run]");
            System.exit(1);
        }

        Path dir = Paths.get(args[0]);
        boolean dryRun = args.length > 1 && "--dry-run".equalsIgnoreCase(args[1]);
        if (!Files.isDirectory(dir)) {
            System.err.println("Not a directory: " + dir);
            System.exit(2);
        }

        DbConfig db = DbConfig.fromEnv();
        if (dryRun) {
            System.out.println("Running in DRY RUN mode. No DB writes.");
        }

        List<Path> files = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".json"))
                    .forEach(files::add);
        }

        if (files.isEmpty()) {
            System.out.println("No JSON files found in: " + dir);
            return;
        }

        String sql = "INSERT INTO jlpt_exam (level, exam_id, part, source_file, json_data) " +
                "VALUES (?, ?, ?, ?, ?::jsonb) " +
                "ON CONFLICT (level, exam_id, part) DO UPDATE SET " +
                "json_data = EXCLUDED.json_data, source_file = EXCLUDED.source_file";

        try (Connection conn = DriverManager.getConnection(db.url, db.user, db.password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            int inserted = 0;
            int skipped = 0;

            for (Path file : files) {
                String name = file.getFileName().toString();
                ExamMeta meta = ExamMeta.fromFilename(name);
                if (meta == null) {
                    System.out.println("Skip (filename not matched): " + name);
                    skipped++;
                    continue;
                }

                JsonNode json = readJsonWithFallback(file);
                if (json == null) {
                    System.out.println("Skip (cannot parse JSON): " + name);
                    skipped++;
                    continue;
                }

                String jsonStr = MAPPER.writeValueAsString(json);
                if (dryRun) {
                    System.out.println("DRY RUN import: " + meta.level + " " + meta.examId + " part " + meta.part + " (" + name + ")");
                    inserted++;
                    continue;
                }

                PGobject jsonb = new PGobject();
                jsonb.setType("jsonb");
                jsonb.setValue(jsonStr);

                stmt.setString(1, meta.level);
                stmt.setString(2, meta.examId);
                stmt.setInt(3, meta.part);
                stmt.setString(4, name);
                stmt.setObject(5, jsonb);
                stmt.addBatch();
                inserted++;
            }

            if (!dryRun) {
                stmt.executeBatch();
                conn.commit();
            }

            System.out.println("Done. Imported: " + inserted + ", skipped: " + skipped + ", total: " + files.size());
        }
    }

    private static JsonNode readJsonWithFallback(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        for (Charset cs : ENCODINGS) {
            try {
                String text = new String(bytes, cs);
                return MAPPER.readTree(text);
            } catch (Exception ignored) {
                // try next encoding
            }
        }
        return null;
    }

    private static class DbConfig {
        final String url;
        final String user;
        final String password;

        private DbConfig(String url, String user, String password) {
            this.url = url;
            this.user = user;
            this.password = password;
        }

        static DbConfig fromEnv() {
            String url = getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/jp_vocab_pg_new");
            String user = getEnvOrDefault("DB_USER", "postgres");
            String pass = getEnvOrDefault("DB_PASSWORD", "Abc12345");
            return new DbConfig(url, user, pass);
        }

        private static String getEnvOrDefault(String key, String def) {
            String value = System.getenv(key);
            return value == null || value.trim().isEmpty() ? def : value.trim();
        }
    }

    private static class ExamMeta {
        final String level;
        final String examId;
        final int part;

        private ExamMeta(String level, String examId, int part) {
            this.level = level;
            this.examId = examId;
            this.part = part;
        }

        static ExamMeta fromFilename(String name) {
            String base = name;
            if (base.endsWith(".json")) {
                base = base.substring(0, base.length() - 5);
            }
            String[] parts = base.split("-");
            if (parts.length != 4) {
                return null;
            }
            String level = parts[0].toUpperCase();
            if (!level.matches("N[1-5]")) {
                return null;
            }
            String year = parts[1];
            String month = parts[2];
            String partStr = parts[3];
            if (!year.matches("\\d{4}") || !month.matches("\\d{1,2}") || !partStr.matches("\\d{1,2}")) {
                return null;
            }
            String examId = year + String.format("%02d", Integer.parseInt(month));
            int part = Integer.parseInt(partStr);
            if (part < 1 || part > 3) {
                return null;
            }
            return new ExamMeta(level, examId, part);
        }
    }
}
