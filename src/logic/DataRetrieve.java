package logic;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
		
		//remove
		new GitBoundary("https://github.com/apache/falcon");
		
		String projectName = "FALCON";
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		TreeMap<String, Integer> counts = new TreeMap<>();
		String date;
		do {
			
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
					+ projectName +"%22AND%22resolution%22=%22fixed%22ORDER%20BY%20resolutiondate%20ASC"
					+"&fields=key,resolutiondate,versions,created&startAt="+ i.toString() +
					"&maxResults=" + j.toString();
			JSONObject json = JSONManager.readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total  = json.getInt("total");
			for(; i < total && i < j; i++) {
				date = issues.getJSONObject(i%1000).getJSONObject("fields").get("resolutiondate").toString().substring(0, 7);
				counts.compute(date, (key, oldValue) -> ((oldValue == null) ? 1 : oldValue+1));
			}
		} while (i < total);	
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
		DataManage dm = new DataManage("Falcon-Process control chart");
		
		dm.createCSV(counts);
	} 
	
}
