public class RetryExample {
    public static void main(String[] args) throws Exception {
        RetryHelper retrier = new RetryHelper(3, 1000); // 3 attempts, 1 second delay
        
        // Operation with return value
        String data = retrier.execute(() -> fetchFromAPI());
        System.out.println("Result: " + data);
        
        // Operation without return value
        retrier.execute(() -> writeToDatabase());
        System.out.println("Success!");
    }
    
    static String fetchFromAPI() throws Exception {
        if (Math.random() < 0.7) {
            throw new RuntimeException("API temporarily unavailable");
        }
        return "Data";
    }
    
    static void writeToDatabase() throws Exception {
        if (Math.random() < 0.6) {
            throw new Exception("Database connection failed");
        }
    }
}
