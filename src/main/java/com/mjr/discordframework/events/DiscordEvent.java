package com.mjr.discordframework.events;

public class DiscordEvent {
	public enum DiscordEventType {
		DEBUG("Debug"), CONNECTED("Connected"), MESSAGE_ERROR("MessageError");

		public final String name;

		DiscordEventType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public DiscordEventType eventType;

	public DiscordEvent(DiscordEventType eventType) {
		super();
		this.eventType = eventType;
	}
}
