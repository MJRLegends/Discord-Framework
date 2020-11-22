package com.mjr.discordframework.managers;

import java.util.ArrayList;
import java.util.List;

import com.mjr.discordframework.events.DiscordEvent;

public class DiscordEventListeners {
	private static List<DiscordEvent> listeners = new ArrayList<DiscordEvent>();

	public static void registerEventHandler(DiscordEvent event) {
		listeners.add(event);
	}

	public static List<DiscordEvent> getEventListeners() {
		return listeners;
	}
}
