
import java.util.*; 
import java.io.*; 

public class PLUTO{
	
	public PLUTO(String filename){
		File file = new File(filename); 
		try{
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
        		String line = scanner.nextLine();
        		System.out.println(line);
        		
        	}
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
	}

	public static void main(String[] args){
		PLUTO map = new PLUTO("toy.csv");
	}
}