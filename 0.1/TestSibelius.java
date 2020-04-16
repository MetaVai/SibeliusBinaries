import java.nio.file.*;

public class TestSibelius {
    public static String readFileAsString(String fileName)throws Exception 
    { 
      String data = ""; 
      data = new String(Files.readAllBytes(Paths.get(fileName))); 
      return data; 
    } 

    public static void main(String argv[]) {
        System.out.println("Working Directory = " +
              System.getProperty("user.dir"));
        System.load(System.getProperty("user.dir") + "/libSibeliusAPI.so");
        try {
            String query = readFileAsString(System.getProperty("user.dir") + "/PricingQuery.json");
            SibeliusAPI.ForceLoadConfig(System.getProperty("user.dir"));
            value v = SibeliusAPI.ParseJson(query);
            value result = SibeliusAPI.callFunction(v);
            System.out.println(SibeliusAPI.ToString(result));
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}