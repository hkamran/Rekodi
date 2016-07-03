package com.hkamran.mocking;

import java.io.IOException;
import java.util.List;

public class Recorder {

	private Tape tape;

	public Recorder(Tape tape) {
		this.setTape(tape);
	}

	public void add(final Request request, final Response response) {
		getTape().put(request, response);
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
