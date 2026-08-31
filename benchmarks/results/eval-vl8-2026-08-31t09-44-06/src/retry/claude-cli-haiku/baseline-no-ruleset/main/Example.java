public class Example {
    public static void main(String[] args) throws Exception {
        RetryHelper retry = new RetryHelper(3, 1000);
        
        // Operation that returns a value
        String result = retry.execute(() -> fetchDataFromApi());
        
        // Void operation
        retry.executeVoid(() -> sendRequest());
        
        // With lambda that may throw
        Integer number = retry.execute(() -> {
            return Integer.parseInt(callRemoteService());
        });
    }
    
    static String fetchDataFromApi() throws Exception {
        return "data";
    }
    
    static void sendRequest() throws Exception {
    }
    
    static String callRemoteService() throws Exception {
        return "123";
    }
}
