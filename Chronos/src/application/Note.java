package application;

public class Note {
	private String _content = "";
	
	public Note(String content) {
		_content = content;
	} 
	
	public String toString(){
		return _content;
	}
}
