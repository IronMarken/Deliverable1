package logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

public class GitBoundary {
	
	private File workingCopy;
	private static final Logger LOGGER = Logger.getLogger(GitBoundary.class.getName());
	
	public GitBoundary(String gitUrl) throws GitAPIException, IOException {
		
		//Parse project name
		String[] splitted = gitUrl.split("/");
		String projectName = splitted[splitted.length -1];
		
		//check repo dir exists
		File localDir = new File ("repo");
		if( !localDir.isDirectory()) {
			if( !localDir.mkdir() )
				LOGGER.log(Level.WARNING, "Dir not created");
			else 
				LOGGER.log(Level.INFO, "Dir created");
		}		
		
		//clone if working copy doesn't exist or pull it
		this.workingCopy = new File("repo/"+ projectName);
		
		if(!this.workingCopy.exists()) {
			//clone
			Git.cloneRepository().setURI(gitUrl).setDirectory(this.workingCopy).call();
			LOGGER.log(Level.INFO,"Project cloned");
		}else
			//pull
			this.pull();		
	}
	
	
	public void pull() throws IOException, GitAPIException {
		
		Repository repo = new FileRepository(this.workingCopy+ "/.git");
		Git git = new Git(repo);
		try {
			PullCommand pullCmd = git.pull();
			PullResult result = pullCmd.call();
		
			if(!result.isSuccessful())
				LOGGER.log(Level.WARNING,"Pull failed");
		
			LOGGER.log(Level.INFO, "Pull successful");
		} finally {
			git.close();
		}
	}
	
	public LocalDateTime getDate(String message) throws IOException{
		//Get exact commit 
		String option = "-"+ message +" ";		
		
		Process process = Runtime.getRuntime().exec(new String[] {"git", "log", "-1", "--pretty=format:%cd", "--date=iso", "--grep="+ option }, null, this.workingCopy);
		BufferedReader reader = new BufferedReader (new InputStreamReader (process.getInputStream()));
		String line;
		String date = null;
		LocalDateTime dateTime;
		
		while((line = reader.readLine()) != null) {
			date = line;
		}
		
		if( date != null) {
			
			//get Date from full line
			date = date.split(" ")[0];
			
			LocalDate ld = LocalDate.parse(date);
			dateTime = ld.atStartOfDay();
		}else dateTime = null;
		
		return dateTime;
	}

}
