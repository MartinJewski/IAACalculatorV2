package Display;
import java.util.List;

public class Show {

	public static void showList(List<String[]> list) {
		
		for(int i = 0; i < list.size() ;i++) {
			for(int j = 0; j < list.get(i).length; j++) {
				System.out.print(list.get(i)[j]);
				System.out.print(" ");
			}
			System.out.println(" ");
		}
	}
	
	public static void showNestedList(List<List<String[]>> list) {
		for(int i = 0; i < list.size(); i++) {
			for(int j = 0; j < list.get(i).size(); j++) {
				for(int k = 0; k <list.get(i).get(j).length; k++) {
					System.out.print(list.get(i).get(j)[k] + " ");
				}
				System.out.println(" ");
			}
			System.out.println("\n");
		}

	}
	
	public static void showListS(List<String> list) {
		
		for(String s: list) {
			System.out.println(s);
		}
		
	}
	
	public static void showNestedListS(List<List<String>> list) {
		
		for(List<String> ls : list) {
			
			for(String s : ls) {
				System.out.print(s);
			}
			System.out.println(" ");
		}
		
	}
}
