package com.hkamran.mocking;

import java.io.IOException;

public class Recorder {

	Request curRequest;
	private Tape tape;
	
	public Recorder(Tape tape) {
		this.setTape(tape);
	}
	
	public void setCurrentRequest(final Request request) {
		this.curRequest = request;
	}
	
	public void save(final Response response) {
		getTape().put(curRequest, response);
	}

	public Tape getTape() {
		return tape;
	}

	public void setTape(Tape tape) {
		this.tape = tape;
	}
	
	public void exportTape(String path) throws IOException {
		this.tape.export(path);
	}
	
	
}
