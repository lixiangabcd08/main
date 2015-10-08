package application;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
public class Logic {

	private boolean _isExiting = false;
	private boolean _isInSummaryView = true;
	private Storage _store;
	private Preferences _userPrefs;
	
	//TODO: tell GUI to have an extra column for notes
	//TODO: Prefs: store latest ID's from tasks and events
	
	public Logic(){
		_userPrefs = Preferences.userNodeForPackage(this.getClass());
	}
	
	public Feedback executeUserCommand(String userInput) {
			
		Command aCommand = new Command(userInput, _store);
		Feedback feedback = aCommand.execute();
		
		//Check for boolean values
		if (aCommand.isExiting()) {
			_isExiting = true;
		}
		
		return feedback;
	}

	public boolean isProgramExiting() {
		return _isExiting;
	}
	
	public boolean isSavePresent() {
		String savedPath = _userPrefs.get("path", "none");
		if (savedPath.equals("none")){
			return false;
		} else { //there is a saved path
			_store = new Storage(savedPath);
			return true;
		}
	}

	public Feedback setSavePath(String path) {
		_userPrefs.put("path", path);
		_store = new Storage(path);
		String feedbackString = "Setting save path to: " + _userPrefs.get("path", "none");
		return new Feedback(feedbackString);
	}
	
	public ArrayList<Task> getTasks(){
		ArrayList<Task> entries = new ArrayList<Task>();
		JSONObject entry;
		for (int i = 0; i<_store.entries_.size(); i++){
			entry = (JSONObject)_store.entries_.get(i);
			entries.add(new Task("",(String)entry.get("content"), "", "", ""));
		}
		return entries;
	}
}
