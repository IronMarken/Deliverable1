package logic;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataManage {
	
	private String csvFilename; 
	private static final Logger LOGGER = Logger.getLogger(DataManage.class.getName());
	
	public DataManage() {
		new DataManage("out");
	}
	
	public DataManage(String filename) {
		//check dir exists or create it
		File dir = new File("resources");
		if(!dir.isDirectory())
			dir.mkdir();
		this.csvFilename = "resources/" + filename + ".csv";
				
	}
	
	public String convertToCSV(String[] data) {
		return Stream.of(data).map(this::escapeSpecialCharacters).collect(Collectors.joining(","));
	}

	public String escapeSpecialCharacters(String data) {
		String escapedData = data.replaceAll("\\R", " ");
		if(data.contains(",") || data.contains("\"") || data.contains("'")) {
			data = data.replace("\"", "\"\"");
			escapedData = "\"" + data +"\"";
		}
		return escapedData;
	}
	
	public void toCSV(List<String[]> dataLines) throws IOException{
		File csvOutputFile = new File(csvFilename);
		if(!csvOutputFile.createNewFile()) 
			LOGGER.log(Level.WARNING,"File already exists");
		else {
			try (PrintWriter pw = new PrintWriter (csvOutputFile)){
				dataLines.stream().map(this::convertToCSV).forEach(pw::println);
			}
			LOGGER.log(Level.INFO, "File created");
		}
	}
	
	public void createCSV(SortedMap<String, Integer> map) {
		List<String[]> dataToConvert = new ArrayList<>();
		dataToConvert.add(new String[] {"Month", "Fixed tickets"});
        String date;
		Set<Map.Entry<String, Integer> > entrySet  = map.entrySet(); 
		for(Map.Entry<String, Integer> entry: entrySet) {
			date = entry.getKey();
			dataToConvert.add(new String[] {Months.values()[Integer.parseInt(date.substring(5,7))-1].toString() +" "+ date.substring(0,4), entry.getValue().toString() });
		}
		try {
			this.toCSV(dataToConvert);
	
		}catch(IOException e) {
			e.printStackTrace();
		}
	}	
	
}
