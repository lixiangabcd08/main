package application;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;
import org.json.simple.JSONObject;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.utils.Span;

public class UpdateCommand extends Command {

	//Unique attributes
	protected JSONObject _oldEntry;
	protected int _id;
		
	//Constant Strings
	protected static final String FEEDBACK_MESSAGE =  "Updated %1$s";
	protected static final String FEEDBACK_MESSAGE_UNDO =  "Restored %1$s";
	static final String JSON_START_DATE = "start date";
	static final String JSON_END_DATE = "due date";
	static final String JSON_ALARM = "alarm";
	static final String OFF_ALARM = "off";
	static final String ALARM_COMMAND = "%1$s, %2$s";
	static final String ERROR_PARSING_ALARM = "failed to parse dates for alarm";
	static final int HOUR_TO_MILLI = 1000*60*60;
	
	protected static final int LIMIT_ID = -1;
	
	public UpdateCommand(String content) {
		super(content);
	}

	@Override
	public Feedback execute() { 
		ArrayList<String> updateDetails = _parse.parseUpdateString(_content);
		String taskID = updateDetails.get(0);
		_id = findEntry(taskID);
		if (_id > LIMIT_ID) {
			_store.storeTemp();
			JSONObject entry = (JSONObject) _store.entries_.get(_id);
			updateEntry(entry, updateDetails);
			_store.storeChanges();
			String feedbackString = String.format(FEEDBACK_MESSAGE, _content);
			return new Feedback(feedbackString);
		} else {
			return new Feedback(ERROR_INVALID_ID);
		}
	}

	protected int findEntry(String id) {
		for (int i = 0; i < _store.entries_.size(); i++) {
			JSONObject currentEntry = (JSONObject) _store.entries_.get(i);
			if (currentEntry.get(Parser.JSON_ID).equals(id)) {
				_oldEntry = (JSONObject) currentEntry.clone();
				return i;
			}
		}
		return -1;
	}
	
	protected void updateEntry(JSONObject entry, ArrayList<String> updateDetails) {
		String field,value;
		Span aSpan;
		DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy HH:mm");
		String id = entry.get(_parse.JSON_ID).toString();
		boolean isChanged = false;
		int offset = checkAlarmOffset(entry, dateFormat);
		for (int j=1; j<updateDetails.size();j++){
			field = updateDetails.get(j);
			value = updateDetails.get(++j);
			if (field.equals(JSON_END_DATE)||field.equals(JSON_START_DATE)){
				//if there is a change in dates, we have to update the alarm
				isChanged = true;
				aSpan = Chronic.parse(value);	
				value = dateFormat.format(aSpan.getBeginCalendar().getTime());
			}
			id = updateEntry(entry, field, value, id);
		}
		//update the alarm if there is a need to do so
		if (isChanged&&offset != -1){
			AlarmCommand setAlarm = new AlarmCommand(String.format(ALARM_COMMAND, id, offset));
			setAlarm.execute();
		}
	}

	private String updateEntry(JSONObject entry, String field, String value,
			String id) {
		if(entry.get(JSON_START_DATE) == null &&field.equals(JSON_START_DATE)) {//convert task to event
				Event event = taskToEvent(entry, value);
				id = event.getId();
				DeleteCommand deleteTask = new DeleteCommand(entry.get(_parse.JSON_ID).toString());
				deleteTask.execute();
				_store.entries_.add(_parse.convertToJSON(event));
		}else {			//for event and  updating task as usual
			entry.replace(field,value);
			_store.entries_.set(_id, entry);
		}
		return id;
	}

	private int checkAlarmOffset(JSONObject entry, DateFormat dateFormat) {
		try{
			//check if the entry has alarm
			if(entry.get(JSON_ALARM) != OFF_ALARM){
				Date alarmFrom;
				//if the entry has alarm, check how many hours prior the user wants the alarm to go off
				if(entry.get(JSON_START_DATE)!=null){
					//if it is event, calculate from the start of the event
					alarmFrom = dateFormat.parse(entry.get(JSON_START_DATE).toString());
				}else{
					//if it is task, calculate from the end of the event
					alarmFrom = dateFormat.parse(entry.get(JSON_END_DATE).toString());
				}
				Date alarm = dateFormat.parse(entry.get(JSON_ALARM).toString());
				return (int)( alarmFrom.getTime() - alarm.getTime())/HOUR_TO_MILLI;
			}else{
				return -1;
			}
		}catch (ParseException e){
			log.info(ERROR_PARSING_ALARM);
			return -1;
		}
	}
	
	private Event taskToEvent(JSONObject task, String value) {
		String desc = task.get(_parse.JSON_DESC).toString();
		String endDate = task.get(_parse.JSON_END_DATE).toString();
		String priority = task.get(_parse.JSON_PRIORITY).toString();
		String category = task.get(_parse.JSON_CATEGORY).toString();

		//Event event = new Event(EMPTY, desc, EMPTY, endDate, priority, category, null);

		String alarm = task.get(_parse.JSON_ALARM).toString();
		Event event = new Event(EMPTY, desc, EMPTY, endDate, priority, category,alarm);
		event.setStartDate(value);
		event.setId(_store.getEventId());
		return event;
	}

	@Override
	public Feedback undo() {
		_store.storeTemp();
		JSONObject entry = (JSONObject) _store.entries_.get(_id);
		_store.entries_.set(_id, _oldEntry);
		_store.storeChanges();
		String feedbackString = String.format(FEEDBACK_MESSAGE_UNDO, _content);
		return new Feedback(feedbackString);
	}
	
}
