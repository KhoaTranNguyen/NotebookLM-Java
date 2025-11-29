package knockbooklm.model;

public class DatabaseConfig {
    public String url() {
        return getenvOr("DB_URL", "jdbc:mysql://localhost:3306/chatbotjava");
    }
    public String username() {
        return getenvOr("DB_USER", "root");
    }
    public String password() {
        // Fallback to the password used by the original Spring config
        return getenvOr("DB_PASS", "Nick@5439");
    }
    private String getenvOr(String key, String def) {
        String v = System.getenv(key);
        return v == null || v.isBlank() ? def : v;
    }
}
