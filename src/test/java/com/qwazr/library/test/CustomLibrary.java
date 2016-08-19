package com.qwazr.library.test;

import com.qwazr.library.LibraryInterface;

import java.util.concurrent.atomic.AtomicBoolean;

public class CustomLibrary implements LibraryInterface {

	private final AtomicBoolean loaded = new AtomicBoolean(false);

	public final Integer myParam = null;

	public void load() {
		loaded.set(true);
	}

	public boolean isLoaded() {
		return loaded.get();
	}
}
