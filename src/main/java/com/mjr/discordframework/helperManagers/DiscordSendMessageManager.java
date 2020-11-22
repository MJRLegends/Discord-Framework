package com.mjr.discordframework.helperManagers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.mjr.discordframework.DiscordBotBase;
import com.mjr.discordframework.DiscordEventHooks;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.*;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

public class DiscordSendMessageManager {
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

	/**
	 * Send a private message to a user
	 *
	 * @param user
	 * @param message
	 */
	public static void sendPrivateMessage(DiscordClient client, Mono<User> user, String message) {
		sendPrivateMessage(client, user.block(), message);
	}

	/**
	 * Send a private message to a user
	 *
	 * @param user
	 * @param message
	 */
	public static void sendPrivateMessage(DiscordClient client, User user, String message) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send message to User: " + user.getUsername() + " Message: " + message);
			user.getPrivateChannel().block().createMessage(message).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Private Message could not be sent, error: " + error.getMessage());
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, ":warning: unable to send message to user " + user.getUsername());
			}).subscribe();
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Private Message could not be sent, error: " + e.getMessage());
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, ":warning: unable to send message of ```" + message + "```" + " to user " + user.getUsername());
		}
	}

	/**
	 * Send a message to a channel, returns a Message object
	 *
	 * @param channel
	 * @param message
	 * @return
	 */
	public Message sendMessageMC(DiscordClient client, Mono<MessageChannel> channel, String message) {
		return sendMessage(client, channel.ofType(Channel.class), message);
	}

	/**
	 * Send a message to a channel, returns a Message object
	 *
	 * @param channel
	 * @param message
	 * @return
	 */
	public static Message sendMessage(DiscordClient client, Mono<Channel> channel, String message) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			return messageReturn.block();
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Send a message to a channel, returns a Mono<Message> object
	 *
	 * @param channel
	 * @param message
	 * @return
	 */
	public Mono<Message> sendMessageMCReturnMonoMsg(DiscordClient client, Mono<MessageChannel> channel, String message) {
		return sendMessageReturnMonoMsg(client, channel.ofType(Channel.class), message);
	}

	/**
	 * Send a message to a channel, returns a Mono<Message> object
	 *
	 * @param channel
	 * @param message
	 * @return
	 */
	public Mono<Message> sendMessageReturnMonoMsg(DiscordClient client, Mono<Channel> channel, String message) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			messageReturn.subscribe();
			return messageReturn;
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Send a embedded message to a channel, returns a Message object
	 *
	 * @param channel
	 * @param builder
	 * @return
	 */
	public Message sendEmbeddedMessageMC(DiscordClient client, Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder) {
		return sendEmbeddedMessage(client, channel.ofType(Channel.class), builder);
	}

	/**
	 * Send a embedded message to a channel, returns a Message object
	 *
	 * @param channel
	 * @param builder
	 * @return
	 */
	public static Message sendEmbeddedMessage(DiscordClient client, Mono<Channel> channel, Consumer<EmbedCreateSpec> builder) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(spec -> spec.setEmbed(builder)).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			return messageReturn.block();
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Send a embedded message to a channel, returns a Message object
	 *
	 * @param channel
	 * @param builder
	 * @return
	 */
	public Message sendEmbeddedWithNormalMessageMC(DiscordClient client, Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder, String message) {
		return sendEmbeddedWithNormalMessage(client, channel.ofType(Channel.class), builder, message);
	}

	/**
	 * Send a embedded message to a channel, returns a Message object
	 *
	 * @param channel
	 * @param builder
	 * @return
	 */
	public static Message sendEmbeddedWithNormalMessage(DiscordClient client, Mono<Channel> channel, Consumer<EmbedCreateSpec> builder, String message) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(spec -> spec.setEmbed(builder).setContent(message)).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			return messageReturn.block();
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Send a embedded message to a channel, returns a Mono<Message> object
	 *
	 * @param channel
	 * @param builder
	 * @return
	 */
	public Mono<Message> sendEmbeddedMessageMCReturnMonoMsg(DiscordClient client, Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder) {
		return sendEmbeddedMessageReturnMonoMsg(client, channel.ofType(Channel.class), builder);
	}

	/**
	 * Send a embedded message to a channel, returns a Mono<Message> object
	 *
	 * @param channel
	 * @param builder
	 * @return
	 */
	private Mono<Message> sendEmbeddedMessageReturnMonoMsg(DiscordClient client, Mono<Channel> channel, Consumer<EmbedCreateSpec> builder) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(spec -> spec.setEmbed(builder)).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			messageReturn.subscribe();
			return messageReturn;
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}


	/**
	 * Send a timed message to a channel
	 *
	 * @param channel
	 * @param message
	 */
	public static void sendTimedMessage(DiscordClient client, DiscordBotBase botBase, Mono<Channel> channel, String message) {
		sendTimedMessage(client, botBase, channel, message, 1L, TimeUnit.MINUTES);
	}

	/**
	 * Send a timed message to a channel
	 *
	 * @param channel
	 * @param message
	 * @param delay
	 * @param timeUnit
	 */
	public static void sendTimedMessageMC(DiscordClient client, DiscordBotBase botBase, Mono<MessageChannel> channel, String message, Long delay, TimeUnit timeUnit) {
		sendTimedMessage(client, botBase, channel.ofType(Channel.class), message, delay, timeUnit);
	}

	/**
	 * Send a timed message to a channel
	 *
	 * @param channel
	 * @param message
	 */
	public static void sendTimedMessageMC(DiscordClient client, DiscordBotBase botBase, Mono<MessageChannel> channel, String message) {
		sendTimedMessage(client, botBase, channel.ofType(Channel.class), message);
	}

	/**
	 * Send a timed message to a channel
	 *
	 * @param channel
	 * @param message
	 * @param delay
	 * @param timeUnit
	 */
	public static void sendTimedMessage(DiscordClient client, DiscordBotBase botBase, Mono<Channel> channel, String message, Long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Message lastMessage = sendMessage(client, channel, message);
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
	 * Send a timed embedded message to a channel
	 *
	 * @param channel
	 * @param builder
	 * @param delay
	 * @param timeUnit
	 */
	public static void sendTimedEmbeddedMessageMC(DiscordClient client, DiscordBotBase botBase, Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder, long delay, TimeUnit timeUnit) {
		sendTimedEmbeddedMessage(client, botBase, channel.ofType(Channel.class), builder, delay, timeUnit);
	}

	/**
	 * Send a timed embedded message to a channel
	 *
	 * @param channel
	 * @param builder
	 * @param delay
	 * @param timeUnit
	 */
	public static void sendTimedEmbeddedMessage(DiscordClient client, DiscordBotBase botBase, Mono<Channel> channel, Consumer<EmbedCreateSpec> builder, long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Message lastMessage = sendEmbeddedMessage(client, channel, builder);
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
