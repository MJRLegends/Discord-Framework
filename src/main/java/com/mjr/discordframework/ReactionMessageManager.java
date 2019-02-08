package com.mjr.discordframework;

import java.util.HashMap;
import java.util.Map;

import com.mjr.discordframework.messageTypes.ReactionEmbeddedMessage;
import com.mjr.discordframework.messageTypes.ReactionMessage;

import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;

public class ReactionMessageManager {
	private Map<Message, ReactionMessage> reactionMessages = new HashMap<Message, ReactionMessage>();
	private Map<Message, ReactionEmbeddedMessage> reactionEmbeddedMessages = new HashMap<Message, ReactionEmbeddedMessage>();

	public Map<Message, ReactionMessage> getReactionMessages() {
		return reactionMessages;
	}

	public ReactionMessage getReactionMessageByMessageID(Snowflake messageID) {
		for (Message message : reactionMessages.keySet()) {
			if (message.getId().equals(messageID))
				return reactionMessages.get(message);
		}
		return null;
	}

	public ReactionMessage getReactionMessageByMessageID(Long messageID) {
		for (Message message : reactionMessages.keySet()) {
			if (message.getId().asLong() == messageID)
				return reactionMessages.get(message);
		}
		return null;
	}

	public ReactionEmbeddedMessage getReactionEmbeddedMessageByMessageID(Snowflake messageID) {
		for (Message message : reactionMessages.keySet()) {
			if (message.getId().equals(messageID))
				return reactionEmbeddedMessages.get(message);
		}
		return null;
	}

	public ReactionEmbeddedMessage getReactionEmbeddedMessageByMessageID(Long messageID) {
		for (Message message : reactionMessages.keySet()) {
			if (message.getId().asLong() == messageID)
				return reactionEmbeddedMessages.get(message);
		}
		return null;
	}

	public void setReactionMessages(Map<Message, ReactionMessage> reactionMessages) {
		this.reactionMessages = reactionMessages;
	}

	public void addReactionMessage(Message message, ReactionMessage reactionMessage) {
		this.reactionMessages.put(message, reactionMessage);
	}

	public void removeReactionMessage(Message message) {
		this.reactionMessages.remove(message);
	}

	public void setReactionEmbeddedMessages(Map<Message, ReactionEmbeddedMessage> reactionEmbeddedMessages) {
		this.reactionEmbeddedMessages = reactionEmbeddedMessages;
	}

	public void addReactionEmbeddedMessage(Message message, ReactionEmbeddedMessage reactionMessage) {
		this.reactionEmbeddedMessages.put(message, reactionMessage);
	}

	public void removeReactionEmbeddedMessage(Message message) {
		this.reactionEmbeddedMessages.remove(message);
	}

}
