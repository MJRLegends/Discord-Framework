package com.mjr.discordframework.helperManagers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.mjr.discordframework.DiscordBotBase;
import com.mjr.discordframework.DiscordEventHooks;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DiscordDeleteMessageManager {
	/**
	 * Delete all messages from a channel
	 *
	 * @param channel
	 */
	public static void deleteAllMessagesInMessageChannel(DiscordClient client, DiscordBotBase botBase, Mono<MessageChannel> channel) {
		deleteAllMessagesInChannel(client, botBase, channel.ofType(TextChannel.class));
	}

	/**
	 * Delete all messages from a channel
	 *
	 * @param channel
	 */
	public static void deleteAllMessagesInChannel(DiscordClient client, DiscordBotBase botBase, Mono<TextChannel> channel) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		TextChannel textChannel = channel.block();
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Attempting to run a nuke of all messages on Channel: " + channel.ofType(TextChannel.class).block().getName());

			List<Snowflake> messagesIDS = new ArrayList<Snowflake>();
			Flux<Message> messages = textChannel.getMessagesBefore(Snowflake.of(Instant.now()));
			for (Message temp : messages.collectList().block()) {
				messagesIDS.add(temp.getId());
				botBase.getReactionMessageManager().removeEmbeddedMessage(temp);
			}
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Deleting Bulk Messages from " + textChannel.getName());
			textChannel.bulkDelete(Flux.fromIterable(messagesIDS)).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Channel could not be nuked of messages due to: " + error.getMessage());
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, ":warning: unable to nuke all messages from " + textChannel.getName() + " due to an error, please check the log for details!");
			}).subscribe();
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Channel could not be nuked of messages due to: " + e.getMessage());
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, ":warning: unable to nuke all messages from " + textChannel.getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * Bulk delete messages from a channel
	 *
	 * @param channel
	 * @param messagesToDelete
	 * @param reason
	 */
	public static void deleteAllMessagesInMessageChannel(DiscordClient client, DiscordBotBase botBase, Mono<MessageChannel> channel, List<Message> messagesToDelete, String reason) {
		deleteAllMessagesInChannel(client, botBase, channel.ofType(TextChannel.class), messagesToDelete, reason);
	}

	/**
	 * Bulk delete messages from a channel
	 *
	 * @param channel
	 * @param messagesToDelete
	 * @param reason
	 */
	public static void deleteAllMessagesInChannel(DiscordClient client, DiscordBotBase botBase, Mono<TextChannel> channel, List<Message> messagesToDelete, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		TextChannel textChannel = channel.block();
		try {
			List<Snowflake> messagesIDS = new ArrayList<Snowflake>();
			for (Message temp : messagesToDelete) {
				messagesIDS.add(temp.getId());
				botBase.getReactionMessageManager().removeEmbeddedMessage(temp);
			}
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Deleting Bulk Messages from " + textChannel.getName());
			textChannel.bulkDelete(Flux.fromIterable(messagesIDS)).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Bulk Messages could not be deleted from channel  " + textChannel.getName() + " due to: " + error.getMessage());
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, ":warning: unable to delete a messages in " + textChannel.getName() + " due to an error, please check the log for details!");
			}).subscribe();
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Bulk Messages could not be deleted, error: " + e.getMessage());
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, ":warning: unable to delete a messages in " + textChannel.getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * Delete a message from a channel
	 *
	 * @param message
	 */
	public static void deleteMessage(DiscordClient client, DiscordBotBase botBase, Mono<Message> message, String reason) {
		deleteMessage(client, botBase, message.block(), reason);
	}

	/**
	 * Delete a message from a channel
	 *
	 * @param message
	 */
	public static void deleteMessage(DiscordClient client, DiscordBotBase botBase, Message message, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Deleting message with id: " + message.getId() + " from " + message.getChannel().ofType(TextChannel.class).block().getName());
			message.delete(reason).doOnError(error -> {
				DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be deleted, error: " + error.getMessage());
				DiscordEventHooks.triggerMessageEvent(client,
						DiscordBotBase.DiscordMessageType.Error,
						":warning: unable to delete a message in " + message.getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
			}).block();
			botBase.getReactionMessageManager().removeEmbeddedMessage(message);
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be deleted, error: " + e.getMessage());
			DiscordEventHooks
					.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, ":warning: unable to delete a message in " + message.getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * Delete a message from a channel
	 *
	 * @param channel
	 * @param messageID
	 */
	public static void deleteMessageFromChannel(DiscordClient client, DiscordBotBase botBase, Mono<Channel> channel, Snowflake messageID, String reason) {
		deleteMessageFromChannel(client, botBase, channel, client.getMessageById(channel.block().getId(), messageID), reason);
	}

	/**
	 * Delete a message from a channel
	 *
	 * @param channel
	 * @param messageID
	 */
	public static void deleteMessageFromMessageChannel(DiscordClient client, DiscordBotBase botBase, Mono<MessageChannel> channel, Snowflake messageID, String reason) {
		deleteMessageFromChannel(client, botBase, channel.ofType(Channel.class), client.getMessageById(channel.block().getId(), messageID), reason);
	}

	/**
	 * Delete a message from a channel
	 *
	 * @param channel
	 * @param messageID
	 */
	public static void deleteMessageFromTextChannel(DiscordClient client, DiscordBotBase botBase, Mono<TextChannel> channel, Snowflake messageID, String reason) {
		deleteMessageFromChannel(client, botBase, channel.ofType(Channel.class), client.getMessageById(channel.block().getId(), messageID), reason);
	}

	/**
	 * Delete a message from a channel
	 *
	 * @param channel
	 * @param message
	 */
	public static void deleteMessageFromChannel(DiscordClient client, DiscordBotBase botBase, Mono<Channel> channel, Mono<Message> message, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Info, "Deleting message with id: " + message.block().getId() + " from " + channel.ofType(TextChannel.class).block().getName());
			channel.ofType(TextChannel.class).block().getMessageById(message.block().getId()).block().delete(reason).block();
			botBase.getReactionMessageManager().removeEmbeddedMessage(message.block());
		} catch (Exception e) {
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, "Message could not be deleted, error: " + e.getMessage());
			DiscordEventHooks.triggerMessageEvent(client, DiscordBotBase.DiscordMessageType.Error, ":warning: unable to delete a message in " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}
}
