package gui;

import java.io.IOException;
import java.util.ArrayList;

import application.Command;
import application.Feedback;
import application.Logic;
import application.Task;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GUI extends Application {

	private static final String WINDOW_TITLE = "Chronos V0.1";
	private static final String MESSAGE_WELCOME = "Welcome to Chronos V0.1! Where would you like Chronos to store your tasks and events?";
	private static final String MESSAGE_LOADED = "Welcome to Chronos V0.1! Add a task to get started.";
	
	private static final String ROOT_LAYOUT_FXML = "RootLayout.fxml";
	
	private static final int DATA_FIRST = 0;
	
	private static final int EXIT_NORMAL = 0;
	
	private BorderPane rootLayout;
	private Logic logic;
	private static CommandBarController commandBarController = null;
	private static Summary summary = null;
	private static DetailedView detailView = null;
	
	private boolean _isNewUser;
	private ObservableList<Task> events = FXCollections.observableArrayList();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		initRootLayout();
		initPrimaryStage(primaryStage);
		initLogic();

		addCommandBar(this);
		addSummary(this);
		
		//check if savefile exists
		if (logic.isSavePresent()) {
			_isNewUser = false;
			updateFeedback(logic.executeUserCommand(Command.COMMAND_DISPLAY_D)); 
			commandBarController.displayFeedback(MESSAGE_LOADED);
		} else {
			_isNewUser = true;
			initNewUser();
		}
	}

	private void initNewUser() {
		commandBarController.displayFeedback(MESSAGE_WELCOME);
		summary.setVisible(false);
	}

	private void addDetailView(GUI gui, ArrayList<Task> data) throws IOException {
		detailView = new DetailedView(this);
		rootLayout.setCenter(detailView);
		Task taskToView = data.get(DATA_FIRST);
		detailView.display(taskToView.getDescription(), taskToView.getNote());
	}

	private void initLogic() {
		logic = new Logic();
	}

	private void addSummary(GUI gui) throws IOException {
		summary = new Summary(this);
		rootLayout.setCenter(summary);
	}

	/*
	private ObservableList<Task> getEvents() {
		ArrayList<Task> entries = logic.getTasks();
		for (int i = 0; i < entries.size(); i++){
			events.add(entries.get(i));
		}
		return events;
	}
	*/
	private void addCommandBar(GUI gui) throws IOException {
		commandBarController = new CommandBarController(gui);
		rootLayout.setTop(commandBarController);
	}

	private void initRootLayout() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ROOT_LAYOUT_FXML));
		rootLayout = loader.load();
	}

	private void initPrimaryStage(Stage primaryStage) {
		primaryStage.setTitle(WINDOW_TITLE);

		Scene scene = new Scene(rootLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public void handleCommand(String text) throws IOException {
		if (_isNewUser) {
			updateFeedback(logic.setSavePath(text));
			summary.setVisible(true);
			_isNewUser = false;
		} else {
			Feedback commandFeedback = logic.executeUserCommand(text);
			if (logic.isProgramExiting()) {
				System.exit(EXIT_NORMAL);
			}
			updateFeedback(commandFeedback);
		}
	}

	//get items arrayList from Logic and print them out
	private void updateSummary(ArrayList<Task> eventList) {
		events = FXCollections.observableArrayList(eventList);
		summary.display(events);
	}

	private void updateFeedback(Feedback feedback) throws IOException {
		//choose between summary or detail view
		if (logic.isInSummaryView()) {
			addSummary(this);
		} else {
			addDetailView(this, feedback.getData());
		}
		if (feedback.hasData()) {
			updateSummary(feedback.getData());
		} else {
			//update display
			updateFeedback(logic.executeUserCommand(Command.COMMAND_DISPLAY_D));
		}
		commandBarController.displayFeedback(feedback.getMessage());
	}
}
