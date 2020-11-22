package com.mjr.discordframework.reactionMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mjr.discordframework.DiscordBotBase;
import com.mjr.discordframework.DiscordEventHooks;
import com.mjr.discordframework.helperManagers.DiscordDeleteMessageManager;
import com.mjr.discordframework.helperManagers.DiscordSendMessageManager;
import com.mjr.discordframework.reactionMessage.messageTypes.ReactionEmbeddedMessage;
import com.mjr.discordframework.reactionMessage.messageTypes.ReactionMessage;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public class DiscordReactionMsgSendManager {

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

	/**
	 * Send a message with reactions functions to a channel
	 *
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public Message sendReactionMessageMC(DiscordClient client, DiscordBotBase botBase, ReactionMessage reactionMessage, Mono<MessageChannel> channel) {
		return sendReactionMessage(client, botBase, reactionMessage, channel.ofType(Channel.class));
	}

	/**
	 * Send a message with reactions functions to a channel
	 *
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public static Message sendReactionMessage(DiscordClient client, DiscordBotBase botBase, ReactionMessage reactionMessage, Mono<Channel> channel) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		String message = reactionMessage.getMessage();
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			Message temp = messageReturn.block();
			for (String reactionDefault : reactionMessage.getReactions())
				temp.addReaction(ReactionEmoji.unicode(reactionDefault)).block();
			botBase.getReactionMessageManager().addReactionMessage(temp, reactionMessage);
			return temp;
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Send a message with reactions functions to a channel
	 *
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public Message sendReactionEmbeddedMessageMC(DiscordClient client, DiscordBotBase botBase, ReactionEmbeddedMessage reactionMessage, Mono<MessageChannel> channel) {
		return sendReactionEmbeddedMessage(client, botBase, reactionMessage, channel.ofType(Channel.class));
	}

	/**
	 * Send a message with reactions functions to a channel
	 *
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public static Message sendReactionEmbeddedMessage(DiscordClient client, DiscordBotBase botBase, ReactionEmbeddedMessage reactionMessage, Mono<Channel> channel) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			Message temp = DiscordSendMessageManager.sendEmbeddedMessage(client, channel, reactionMessage.getMessage());
			for (String reactionDefault : reactionMessage.getReactions())
				temp.addReaction(ReactionEmoji.unicode(reactionDefault)).block();
			botBase.getReactionMessageManager().addReactionEmbeddedMessage(temp, reactionMessage);
			return temp;
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Send a timed reaction embedded message to a channel
	 *
	 * @param reactionMessage
	 * @param channel
	 * @param delay
	 * @param timeUnit
	 */
	public static void sendTimedReactionMessageMC(DiscordClient client, DiscordBotBase botBase, ReactionMessage reactionMessage, Mono<MessageChannel> channel, long delay, TimeUnit timeUnit) {
		sendTimedReactionMessage(client, botBase, reactionMessage, channel.ofType(Channel.class), delay, timeUnit);
	}

	/**
	 * Send a timed reaction message to a channel
	 *
	 * @param reactionMessage
	 * @param channel
	 * @param delay
	 * @param timeUnit
	 */
	public static void sendTimedReactionMessage(DiscordClient client, DiscordBotBase botBase, ReactionMessage reactionMessage, Mono<Channel> channel, long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Message lastMessage = sendReactionMessage(client, botBase, reactionMessage, channel);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					DiscordDeleteMessageManager.deleteMessage(client, botBase, lastMessage, "Timed Message Delete");
				}, delay, timeUnit);
			}
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Timed Message could not be sent, error: " + e.getMessage());
		}
	}

	/**
	 * Send a timed reaction message to a channel
	 *
	 * @param reactionMessage
	 * @param channel
	 * @param delay
	 * @param timeUnit
	 */
	public static void sendTimedReactionEmbeddedMessageMC(DiscordClient client, DiscordBotBase botBase, ReactionEmbeddedMessage reactionMessage, Mono<MessageChannel> channel, long delay, TimeUnit timeUnit) {
		sendTimedReactionEmbeddedMessage(client, botBase, reactionMessage, channel.ofType(Channel.class), delay, timeUnit);
	}

	/**
	 * Send a timed reaction embedded message to a channel
	 *
	 * @param reactionMessage
	 * @param channel
	 * @param delay
	 * @param timeUnit
	 */
	public static void sendTimedReactionEmbeddedMessage(DiscordClient client, DiscordBotBase botBase, ReactionEmbeddedMessage reactionMessage, Mono<Channel> channel, long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Message lastMessage = sendReactionEmbeddedMessage(client, botBase, reactionMessage, channel);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					DiscordDeleteMessageManager.deleteMessage(client, botBase, lastMessage, "Timed Message Delete");
				}, delay, timeUnit);
			}
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Timed Message could not be sent, error: " + e.getMessage());
		}
	}

}
