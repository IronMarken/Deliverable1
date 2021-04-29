package logic;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DataRetrieve {
	
	private static final Logger LOGGER = Logger.getLogger(DataRetrieve.class.getName());
	
	public static void logInfo(String message) {
		LOGGER.log(Level.INFO, message);
	}
	
	public static void listMap(SortedMap<String, Integer> map) {
		Integer total = 0;
        String date;
		Set<Map.Entry<String, Integer> > entrySet  = map.entrySet(); 
		for(Map.Entry<String, Integer> entry: entrySet) {
			date = entry.getKey();
			logInfo( date + " -> " + entry.getValue());
			total += entry.getValue();
		}
		logInfo( "total "+total);
	}


	public static void main(String[] args) throws GitAPIException, IOException, JSONException, ParseException {
		
		String gitUrl = "https://github.com/apache/falcon";
		GitBoundary gb = new GitBoundary(gitUrl);
		String projectName = gitUrl.split("/")[gitUrl.split("/").length -1];
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		TreeMap<String, Integer> counts = new TreeMap<>();
		String key;
		List<LocalDateTime> allDates = new ArrayList<>();
		LocalDateTime retDate;
		
		do {			
			j = i+1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
					+ projectName.toUpperCase() +"%22AND%22resolution%22=%22fixed%22"
					+"&fields=key,resolutiondate,versions,created&startAt="+ i.toString() +
					"&maxResults=" + j.toString();
			JSONObject json = JSONManager.readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total  = json.getInt("total");
			
			
			
			for(; i < total && i < j; i++) {
				key = issues.getJSONObject(i%1000).get("key").toString();				
				retDate = gb.getDate(key.split("-")[1]);
				
				if(retDate != null)
					allDates.add(retDate);
	
			}
		} while (i < total);	
		
		//sort list
		Collections.sort(allDates);
		
		//month count
		String date;
		for(i=0; i < allDates.size(); i++ ) {
			date = allDates.get(i).getYear()+ "-" +String.format("%02d", allDates.get(i).getMonthValue());
			counts.compute(date, (keys, oldValue) -> ((oldValue == null) ? 1 : oldValue+1));
		}
		
		//fill list
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Calendar c = Calendar.getInstance();
		String firstDate = counts.firstKey();
		String actualDate ;
		String lastDate = counts.lastKey();
		c.setTime(sdf.parse(firstDate));
		while(lastDate.compareTo( actualDate = sdf.format(c.getTime())) != 0) {
			counts.computeIfAbsent(actualDate, n -> counts.put(n, 0));
			c.add(Calendar.MONTH, 1);
		}
		
		//generate CSV file
		DataManage dm = new DataManage(projectName +"-Process control chart");
		
		dm.createCSV(counts);
	} 
	
}
