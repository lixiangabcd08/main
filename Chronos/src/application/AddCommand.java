package application;

import java.text.ParseException;

public class AddCommand extends Command {
	
	//Unique attributes
	private String _createdItemID;
	private Task _createdTask;
	
	//Constant Strings
	protected static final String FEEDBACK_MESSAGE =  "Added: %1$s";
	public static final String FEEDBACK_MISSING_DESC = "Error: A task needs a description";
	public static final String FEEDBACK_WRONG_DATE = "Error: Invalid Date";
	public static final String FEEDBACK_WRONG_END_DATE = "Error: end cannot be earlier than start";
	public static final String MESSAGE_INVALID_END = "End date < start date";

	//Instructions
	private static final String PATTERN = "add (description), (date), c:(category), p:(priority)";
	private static final String INSTRUCTION_REQUIRED = "Enter a description";
	private static final String INSTRUCTION_OPTIONAL = "Optional fields: date or a date range (ex. today to tomorrow), priority, category";
	private static final String REQUIRED_FIELD_DESC = "(description)";

	public AddCommand(String content) {
		super(content);
	}

	@Override
	public Feedback execute() {
		String feedbackString;
		try {
			_store.storeTemp();
			_createdTask = createTaskOrEvent();
			_createdItemID = _createdTask.getId();
			_store.entries_.add(_parse.convertToJSON(_createdTask));
			_store.storeChanges();
			feedbackString = String.format(FEEDBACK_MESSAGE, _content);
			return new Feedback(feedbackString);
		} catch (Exception e) {
			feedbackString = FEEDBACK_WRONG_DATE;
			return new Feedback(feedbackString);
		}
	}
	
	private Task createTaskOrEvent() throws Exception {
		Task createdItem = _parse.createItem(_content);
		if (createdItem instanceof Event) {
			createdItem.setId(_store.getEventId());
		} else if (createdItem instanceof Task){
			createdItem.setId(_store.getTaskId());
		} else {
			createdItem.setId(0);
		}
		return createdItem;
	}

	@Override
	public Feedback undo() { //edit this
		DeleteCommand undoAdd = new DeleteCommand(_createdItemID);
		if (_createdItemID.contains(Event.ID_HEADER)){
			_store.decreaseEventID();
		} else if (_createdItemID.contains(Task.ID_HEADER)){
			_store.decreaseTaskID();
		} else {
			//do something
		}
		return undoAdd.execute();
	}

	public static Instruction generateInstruction() {
		Instruction commandInstruction = new Instruction();
		commandInstruction.setCommandPattern(PATTERN);
	    commandInstruction.addToInstructions(INSTRUCTION_REQUIRED);
	    commandInstruction.addToRequiredFields(REQUIRED_FIELD_DESC);
	    commandInstruction.addToInstructions(INSTRUCTION_OPTIONAL);
		return commandInstruction;
	}
	
}
