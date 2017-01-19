package shamsa.mcp.spikesolutions;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import shamsa.cbrs._2_methodclonestructures.MCSTable;
import shamsa.cbrs._3_recommendations.Recommendation;
//this code prints the method names inside a class but it is iseless for me
public class Main {

  public static void main(String[] args) {

    Class aClass = MCSTable.class;

    // Get the methods
    Method[] methods = aClass.getDeclaredMethods();

    // Loop through the methods and print out their names
    for (Method method : methods) {
      System.out.println(method.getName());
    }
    
    String methodStart = "public static void main()";
    System.out.println(getMethodName(methodStart));
    
  }
  
  private static String getMethodName(String methodBody) {
	  String delims = "\n";		
		StringTokenizer st = new StringTokenizer(methodBody, delims);
		String result = "";
		
		while (st.hasMoreElements()) 
		{
			result = st.nextToken().trim();
			if(result.endsWith(")") || result.endsWith("{"))
			{	
				String methodName = result.substring(0, result.indexOf("("));
				//tokenize on space and get last token
				StringTokenizer t = new StringTokenizer(methodName, " ");
			
				while (t.hasMoreElements()) 
				{
					
					
					result = t.nextToken().trim();
				}
			
				break;
			}
		}
		return result;

		
	}
}