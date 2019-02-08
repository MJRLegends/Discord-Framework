package com.mjr;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.mjr.messageTypes.ReactionEmbeddedMessage;
import com.mjr.messageTypes.ReactionMessage;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class Discord_Bot {

	public enum MessageType {
		Info("Info"), Error("Error");

		private final String name;

		MessageType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private DiscordClient client;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);
	private EventDispatcher dispatcher;
	private Map<Message, ReactionMessage> reactionMessages = new HashMap<Message, ReactionMessage>();
	private Map<Message, ReactionEmbeddedMessage> reactionEmbeddedMessages = new HashMap<Message, ReactionEmbeddedMessage>();

	public Discord_Bot(String token) {
		super();
		if (token.length() == 0) {
			onOutputMessage(MessageType.Error, "Missing Discord oAuth Token!");
			return;
		}
		onOutputMessage(MessageType.Info, "Starting Discord bot");
		this.client = connectClient(token);
		this.dispatcher = client.getEventDispatcher();
		this.dispatcher.on(ReactionAddEvent.class).onErrorContinue((t, o) -> this.onOutputMessage(MessageType.Error, "Error while processing ReactionAddEvent Error: " + t.getMessage()))
				.subscribe(o -> ReactionMessageEventHandler.onMessageReactionAddReceivedEvent(o, this));
		this.dispatcher.on(ReactionRemoveEvent.class).onErrorContinue((t, o) -> this.onOutputMessage(MessageType.Error, "Error while processing ReactionRemoveEvent Error: " + t.getMessage()))
				.subscribe(o -> ReactionMessageEventHandler.onMessageReactionRemoveReceivedEvent(o, this));
		onOutputMessage(MessageType.Info, "Finshed starting Discord bot");
	}

	/**
	 * @param token
	 * @return
	 */
	private DiscordClient connectClient(String token) {
		try {
			DiscordClient temp = new DiscordClientBuilder(token).build();
			temp.login().subscribe();
			return temp;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Bot was unable to create a connection, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param channel
	 * @param message
	 * @return
	 */
	public Message sendMessageMessageChannel(Mono<MessageChannel> channel, String message) {
		return sendMessage(channel.ofType(Channel.class), message);
	}

	/**
	 * @param channel
	 * @param message
	 * @return
	 */
	public Message sendMessage(Mono<Channel> channel, String message) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + error.getMessage());
			});
			return messageReturn.block();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param channel
	 * @param builder
	 * @return
	 */
	public Message sendEmbeddedMessageMessageChannel(Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder) {
		return sendEmbeddedMessage(channel.ofType(Channel.class), builder);
	}

	/**
	 * @param channel
	 * @param builder
	 * @return
	 */
	private Message sendEmbeddedMessage(Mono<Channel> channel, Consumer<EmbedCreateSpec> builder) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(spec -> spec.setEmbed(builder)).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + error.getMessage());
			});
			return messageReturn.block();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param channel
	 * @param message
	 * @return
	 */
	public Mono<Message> sendMessageMessageChannelReturnMonoMsg(Mono<MessageChannel> channel, String message) {
		return sendMessageReturnMonoMsg(channel.ofType(Channel.class), message);
	}

	/**
	 * @param channel
	 * @param message
	 * @return
	 */
	public Mono<Message> sendMessageReturnMonoMsg(Mono<Channel> channel, String message) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + error.getMessage());
			});
			messageReturn.subscribe();
			return messageReturn;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param channel
	 * @param builder
	 * @return
	 */
	public Mono<Message> sendEmbeddedMessageMessageChannelReturnMonoMsg(Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder) {
		return sendEmbeddedMessageReturnMonoMsg(channel.ofType(Channel.class), builder);
	}

	/**
	 * @param channel
	 * @param builder
	 * @return
	 */
	private Mono<Message> sendEmbeddedMessageReturnMonoMsg(Mono<Channel> channel, Consumer<EmbedCreateSpec> builder) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(spec -> spec.setEmbed(builder)).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + error.getMessage());
			});
			messageReturn.subscribe();
			return messageReturn;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param user
	 * @param message
	 */
	public void sendDirectMessageToUser(Mono<User> user, String message) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send message to User: " + user.block().getUsername() + " Message: " + message);
			user.block().getPrivateChannel().block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Private Message could not be sent, error: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to send message to user " + user.block().getUsername());
			}).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Private Message could not be sent, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to send message of ```" + message + "```" + " to user " + user.block().getUsername());
		}
	}

	/**
	 * @param channel
	 * @param message
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedMessageMessageChannel(Mono<MessageChannel> channel, String message, Long delay, TimeUnit timeUnit) {
		sendTimedMessage(channel.ofType(Channel.class), message, delay, timeUnit);
	}

	/**
	 * @param channel
	 * @param builder
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedMessage(Mono<Channel> channel, Consumer<EmbedCreateSpec> builder, long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Message lastMessage = sendEmbeddedMessage(channel, builder);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					deleteMessage(lastMessage, "Timed Message Delete");
				}, delay, timeUnit);
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Timed Message could not be sent, error: " + e.getMessage());
		}
	}

	/**
	 * @param channel
	 * @param message
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedMessage(Mono<Channel> channel, String message, Long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Message lastMessage = sendMessage(channel, message);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					deleteMessage(lastMessage, "Timed Message Delete");
				}, delay, timeUnit);
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Timed Message could not be sent, error: " + e.getMessage());
		}
	}

	/**
	 * @param channel
	 * @param message
	 */
	public void sendTimedMessageMessageChannel(Mono<MessageChannel> channel, String message) {
		sendTimedMessage(channel.ofType(Channel.class), message);
	}

	/**
	 * @param channel
	 * @param message
	 */
	public void sendTimedMessage(Mono<Channel> channel, String message) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Message lastMessage = sendMessage(channel, message);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					deleteMessage(lastMessage, "Timed Message Delete");
				}, 1L, TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Timed Message could not be sent, error: " + e.getMessage());
		}
	}

	/**
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public void sendReactionMessage(ReactionMessage reactionMessage, Mono<Channel> channel) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		String message = reactionMessage.getMessage();
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			onOutputMessage(MessageType.Info, "Discord: Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + error.getMessage());
			});
			Message temp = messageReturn.block();
			for(String reactionDefault : reactionMessage.getReactions())
				temp.addReaction(ReactionEmoji.unicode(reactionDefault)).block();
			this.addReactionMessage(temp, reactionMessage);
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return;
		}
	}
	
	
	/**
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public void sendReactionMessageMessageChannel(ReactionMessage reactionMessage, Mono<MessageChannel> channel) {
		sendReactionMessage(reactionMessage, channel.ofType(Channel.class));
	}
	
	/**
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public void sendReactionEmbeddedMessage(ReactionEmbeddedMessage reactionMessage, Mono<Channel> channel) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			Message temp = sendEmbeddedMessage(channel, reactionMessage.getMessage());
			for(String reactionDefault : reactionMessage.getReactions())
				temp.addReaction(ReactionEmoji.unicode(reactionDefault)).block();
			this.addReactionEmbeddedMessage(temp, reactionMessage);
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be sent, error: " + e.getMessage());
			return;
		}
	}
	
	
	/**
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public void sendReactionEmbeddedMessageMessageChannel(ReactionEmbeddedMessage reactionMessage, Mono<MessageChannel> channel) {
		sendReactionEmbeddedMessage(reactionMessage, channel.ofType(Channel.class));
	}

	/**
	 * @param channel
	 */
	public void deleteAllMessagesInMessageChannel(Mono<MessageChannel> channel) {
		deleteAllMessagesInChannel(channel.ofType(Channel.class));
	}

	/**
	 * @param channel
	 */
	public void deleteAllMessagesInChannel(Mono<Channel> channel) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			TextChannel textChannel = channel.ofType(TextChannel.class).block();
			onOutputMessage(MessageType.Info, "Discord: Attempting to run a nuke of all messages on Channel: " + channel.ofType(TextChannel.class).block().getName());
			Flux<Message> messages = textChannel.getMessagesBefore(Snowflake.of(Instant.now()));
			for (Message message : messages.toIterable()) {
				message.delete().doOnError(error -> {
					onOutputMessage(MessageType.Error, "Discord: Channel could not be nuked of messages due to: " + error.getMessage());
					onOutputMessage(MessageType.Error, ":warning: unable to nuke all messages from " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
				}).subscribe();
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Channel could not be nuked of messages due to: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to nuke all messages from " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * @param message
	 */
	public void deleteMessage(Mono<Message> message, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Discord: Deleting message with id: " + message.block().getId() + " from " + message.block().getChannel().ofType(TextChannel.class).block().getName());
			message.block().delete(reason).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + message.block().getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
			}).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + message.block().getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * @param message
	 */
	public void deleteMessage(Message message, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Discord: Deleting message with id: " + message.getId() + " from " + message.getChannel().ofType(TextChannel.class).block().getName());
			message.delete(reason).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + message.getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
			}).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + message.getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * @param channel
	 * @param messageID
	 */
	public void deleteMessage(Mono<Channel> channel, Snowflake messageID, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			deleteMessage(channel, client.getMessageById(channel.block().getId(), messageID), reason);
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * @param channel
	 * @param message
	 */
	public void deleteMessage(Mono<Channel> channel, Mono<Message> message, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Discord: Deleting message with id: " + message.block().getId() + " from " + channel.ofType(TextChannel.class).block().getName());
			channel.ofType(TextChannel.class).block().getMessageById(message.block().getId()).block().delete(reason).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * @param oldMessage
	 * @param newMessage
	 * @return
	 */
	public Message editMessage(Mono<Message> oldMessage, Consumer<MessageEditSpec> newMessage) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			return oldMessage.block().edit(newMessage).block();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be edited, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param oldMessage
	 * @param content
	 * @return
	 */
	public Message editMessage(Mono<Message> oldMessage, final String content) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			return oldMessage.block().edit(spec -> spec.setContent(content)).block();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Discord: Message could not be edited, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @return
	 */
	public DiscordClient getClient() {
		return client;
	}

	/**
	 * @return
	 */
	public EventDispatcher getDispatcher() {
		return dispatcher;
	}

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

	public abstract void onOutputMessage(MessageType type, String message);

	public abstract void setupEvents();
}
