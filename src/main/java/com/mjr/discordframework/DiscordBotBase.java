package com.mjr.discordframework;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.mjr.discordframework.messageTypes.ReactionEmbeddedMessage;
import com.mjr.discordframework.messageTypes.ReactionMessage;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageDeleteEvent;
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

public abstract class DiscordBotBase {

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
	private ReactionMessageManager reactionMessageManager;

	/**
	 * Setup Discord Bot instance, make sure to call {@link #setupEvents()} after creating your instance
	 * 
	 * @param token
	 */
	public DiscordBotBase(String token) {
		super();
		if (token.length() == 0) {
			onOutputMessage(MessageType.Error, "Missing Discord oAuth Token!");
			return;
		}
		onOutputMessage(MessageType.Info, "Discord Bot is starting");
		this.client = connectClient(token);
		this.dispatcher = client.getEventDispatcher();
		this.setReactionMessageManager(new ReactionMessageManager());
		this.dispatcher.on(ReactionAddEvent.class).onErrorContinue((t, o) -> this.onOutputMessage(MessageType.Error, "Error while processing ReactionAddEvent Error: " + t.getMessage()))
				.subscribe(o -> ReactionMessageEventHandler.onMessageReactionAddReceivedEvent(o, this));
		this.dispatcher.on(ReactionRemoveEvent.class).onErrorContinue((t, o) -> this.onOutputMessage(MessageType.Error, "Error while processing ReactionRemoveEvent Error: " + t.getMessage()))
				.subscribe(o -> ReactionMessageEventHandler.onMessageReactionRemoveReceivedEvent(o, this));
		this.dispatcher.on(MessageDeleteEvent.class).onErrorContinue((t, o) -> this.onOutputMessage(MessageType.Error, "Error while processing MessageDeleteEvent Error: " + t.getMessage())).subscribe(o -> GlobalEventHandler.onMessageDelete(o, this));
		onOutputMessage(MessageType.Info, "Discord Bot has been fully started");
	}

	/**
	 * Used locally to create a client connection
	 * 
	 * @param token
	 * @return
	 */
	private DiscordClient connectClient(String token) {
		try {
			DiscordClient temp = new DiscordClientBuilder(token).build();
			temp.login().subscribe();
			return temp;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Bot was unable to create a connection, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Send a private message to a user
	 * 
	 * @param user
	 * @param message
	 */
	public void sendPrivateMessage(Mono<User> user, String message) {
		sendPrivateMessage(user.block(), message);
	}

	/**
	 * Send a private message to a user
	 * 
	 * @param user
	 * @param message
	 */
	public void sendPrivateMessage(User user, String message) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			onOutputMessage(MessageType.Info, "Attempting to send message to User: " + user.getUsername() + " Message: " + message);
			user.getPrivateChannel().block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Private Message could not be sent, error: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to send message to user " + user.getUsername());
			}).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Private Message could not be sent, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to send message of ```" + message + "```" + " to user " + user.getUsername());
		}
	}

	/**
	 * Send a message to a channel, returns a Message object
	 * 
	 * @param channel
	 * @param message
	 * @return
	 */
	public Message sendMessageMC(Mono<MessageChannel> channel, String message) {
		return sendMessage(channel.ofType(Channel.class), message);
	}

	/**
	 * Send a message to a channel, returns a Message object
	 * 
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
			onOutputMessage(MessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			return messageReturn.block();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Message could not be sent, error: " + e.getMessage());
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
	public Mono<Message> sendMessageMCReturnMonoMsg(Mono<MessageChannel> channel, String message) {
		return sendMessageReturnMonoMsg(channel.ofType(Channel.class), message);
	}

	/**
	 * Send a message to a channel, returns a Mono<Message> object
	 * 
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
			onOutputMessage(MessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			messageReturn.subscribe();
			return messageReturn;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Message could not be sent, error: " + e.getMessage());
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
	public Message sendEmbeddedMessageMC(Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder) {
		return sendEmbeddedMessage(channel.ofType(Channel.class), builder);
	}

	/**
	 * Send a embedded message to a channel, returns a Message object
	 * 
	 * @param channel
	 * @param builder
	 * @return
	 */
	public Message sendEmbeddedMessage(Mono<Channel> channel, Consumer<EmbedCreateSpec> builder) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			onOutputMessage(MessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(spec -> spec.setEmbed(builder)).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			return messageReturn.block();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Message could not be sent, error: " + e.getMessage());
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
	public Mono<Message> sendEmbeddedMessageMCReturnMonoMsg(Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder) {
		return sendEmbeddedMessageReturnMonoMsg(channel.ofType(Channel.class), builder);
	}

	/**
	 * Send a embedded message to a channel, returns a Mono<Message> object
	 * 
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
			onOutputMessage(MessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(spec -> spec.setEmbed(builder)).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			messageReturn.subscribe();
			return messageReturn;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Message could not be sent, error: " + e.getMessage());
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
	public Message sendReactionMessageMC(ReactionMessage reactionMessage, Mono<MessageChannel> channel) {
		return sendReactionMessage(reactionMessage, channel.ofType(Channel.class));
	}

	/**
	 * Send a message with reactions functions to a channel
	 * 
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public Message sendReactionMessage(ReactionMessage reactionMessage, Mono<Channel> channel) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		String message = reactionMessage.getMessage();
		if (message.length() > 2000)
			message = message.substring(0, 2000);
		try {
			onOutputMessage(MessageType.Info, "Attempting to send message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Mono<Message> messageReturn = channel.ofType(TextChannel.class).block().createMessage(message).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Message could not be sent, error: " + error.getMessage());
			});
			Message temp = messageReturn.block();
			for (String reactionDefault : reactionMessage.getReactions())
				temp.addReaction(ReactionEmoji.unicode(reactionDefault)).block();
			this.getReactionMessageManager().addReactionMessage(temp, reactionMessage);
			return temp;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Message could not be sent, error: " + e.getMessage());
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
	public Message sendReactionEmbeddedMessageMC(ReactionEmbeddedMessage reactionMessage, Mono<MessageChannel> channel) {
		return sendReactionEmbeddedMessage(reactionMessage, channel.ofType(Channel.class));
	}

	/**
	 * Send a message with reactions functions to a channel
	 * 
	 * @param reactionMessage
	 * @param channel
	 * @return
	 */
	public Message sendReactionEmbeddedMessage(ReactionEmbeddedMessage reactionMessage, Mono<Channel> channel) {
		if (client == null)
			return null;
		if (client.isConnected() == false)
			return null;
		try {
			Message temp = sendEmbeddedMessage(channel, reactionMessage.getMessage());
			for (String reactionDefault : reactionMessage.getReactions())
				temp.addReaction(ReactionEmoji.unicode(reactionDefault)).block();
			this.getReactionMessageManager().addReactionEmbeddedMessage(temp, reactionMessage);
			return temp;
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Message could not be sent, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Send a timed message to a channel
	 * 
	 * @param channel
	 * @param message
	 */
	public void sendTimedMessage(Mono<Channel> channel, String message) {
		sendTimedMessage(channel, message, 1L, TimeUnit.MINUTES);
	}

	/**
	 * Send a timed message to a channel
	 * 
	 * @param channel
	 * @param message
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedMessageMC(Mono<MessageChannel> channel, String message, Long delay, TimeUnit timeUnit) {
		sendTimedMessage(channel.ofType(Channel.class), message, delay, timeUnit);
	}

	/**
	 * Send a timed message to a channel
	 * 
	 * @param channel
	 * @param message
	 */
	public void sendTimedMessageMC(Mono<MessageChannel> channel, String message) {
		sendTimedMessage(channel.ofType(Channel.class), message);
	}

	/**
	 * Send a timed message to a channel
	 * 
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
			onOutputMessage(MessageType.Info, "Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: " + message);
			Message lastMessage = sendMessage(channel, message);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					deleteMessage(lastMessage, "Timed Message Delete");
				}, delay, timeUnit);
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Timed Message could not be sent, error: " + e.getMessage());
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
	public void sendTimedEmbeddedMessageMC(Mono<MessageChannel> channel, Consumer<EmbedCreateSpec> builder, long delay, TimeUnit timeUnit) {
		sendTimedEmbeddedMessage(channel.ofType(Channel.class), builder, delay, timeUnit);
	}

	/**
	 * Send a timed embedded message to a channel
	 * 
	 * @param channel
	 * @param builder
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedEmbeddedMessage(Mono<Channel> channel, Consumer<EmbedCreateSpec> builder, long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Message lastMessage = sendEmbeddedMessage(channel, builder);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					deleteMessage(lastMessage, "Timed Message Delete");
				}, delay, timeUnit);
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Timed Message could not be sent, error: " + e.getMessage());
		}
	}

	/**
	 * Send a timed reaction embedded message to a channel
	 * 
	 * @param channel
	 * @param builder
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedReactionMessageMC(ReactionMessage reactionMessage, Mono<MessageChannel> channel, long delay, TimeUnit timeUnit) {
		sendTimedReactionMessage(reactionMessage, channel.ofType(Channel.class), delay, timeUnit);
	}

	/**
	 * Send a timed reaction message to a channel
	 * 
	 * @param channel
	 * @param builder
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedReactionMessage(ReactionMessage reactionMessage, Mono<Channel> channel, long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Message lastMessage = sendReactionMessage(reactionMessage, channel);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					deleteMessage(lastMessage, "Timed Message Delete");
				}, delay, timeUnit);
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Timed Message could not be sent, error: " + e.getMessage());
		}
	}

	/**
	 * Send a timed reaction message to a channel
	 * 
	 * @param channel
	 * @param builder
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedReactionEmbeddedMessageMC(ReactionEmbeddedMessage reactionMessage, Mono<MessageChannel> channel, long delay, TimeUnit timeUnit) {
		sendTimedReactionEmbeddedMessage(reactionMessage, channel.ofType(Channel.class), delay, timeUnit);
	}

	/**
	 * Send a timed reaction embedded message to a channel
	 * 
	 * @param channel
	 * @param builder
	 * @param delay
	 * @param timeUnit
	 */
	public void sendTimedReactionEmbeddedMessage(ReactionEmbeddedMessage reactionMessage, Mono<Channel> channel, long delay, TimeUnit timeUnit) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Attempting to send timed message to Channel: " + channel.ofType(TextChannel.class).block().getName() + " Message: Embedded Message");
			Message lastMessage = sendReactionEmbeddedMessage(reactionMessage, channel);
			if (lastMessage != null) {
				scheduler.schedule(() -> {
					deleteMessage(lastMessage, "Timed Message Delete");
				}, delay, timeUnit);
			}
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Timed Message could not be sent, error: " + e.getMessage());
		}
	}

	/**
	 * Delete all messages from a channel
	 * 
	 * @param channel
	 */
	public void deleteAllMessagesInMessageChannel(Mono<MessageChannel> channel) {
		deleteAllMessagesInChannel(channel.ofType(TextChannel.class));
	}

	/**
	 * Delete all messages from a channel
	 * 
	 * @param channel
	 */
	public void deleteAllMessagesInChannel(Mono<TextChannel> channel) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		TextChannel textChannel = channel.block();
		try {
			onOutputMessage(MessageType.Info, "Attempting to run a nuke of all messages on Channel: " + channel.ofType(TextChannel.class).block().getName());

			List<Snowflake> messagesIDS = new ArrayList<Snowflake>();
			Flux<Message> messages = textChannel.getMessagesBefore(Snowflake.of(Instant.now()));
			for (Message temp : messages.collectList().block()) {
				messagesIDS.add(temp.getId());
				this.getReactionMessageManager().removeEmbeddedMessage(temp);
			}
			onOutputMessage(MessageType.Info, "Deleting Bulk Messages from " + textChannel.getName());
			textChannel.bulkDelete(Flux.fromIterable(messagesIDS)).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Channel could not be nuked of messages due to: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to nuke all messages from " + textChannel.getName() + " due to an error, please check the log for details!");
			}).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Channel could not be nuked of messages due to: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to nuke all messages from " + textChannel.getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * Bulk delete messages from a channel
	 * 
	 * @param message
	 */
	public void deleteAllMessagesInMessageChannel(Mono<MessageChannel> channel, List<Message> messagesToDelete, String reason) {
		deleteAllMessagesInChannel(channel.ofType(TextChannel.class), messagesToDelete, reason);
	}

	/**
	 * Bulk delete messages from a channel
	 * 
	 * @param message
	 */
	public void deleteAllMessagesInChannel(Mono<TextChannel> channel, List<Message> messagesToDelete, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		TextChannel textChannel = channel.block();
		try {
			List<Snowflake> messagesIDS = new ArrayList<Snowflake>();
			for (Message temp : messagesToDelete) {
				messagesIDS.add(temp.getId());
				this.getReactionMessageManager().removeEmbeddedMessage(temp);
			}
			onOutputMessage(MessageType.Info, "Deleting Bulk Messages from " + textChannel.getName());
			textChannel.bulkDelete(Flux.fromIterable(messagesIDS)).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Bulk Messages could not be deleted from channel  " + textChannel.getName() + " due to: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to delete a messages in " + textChannel.getName() + " due to an error, please check the log for details!");
			}).subscribe();
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Bulk Messages could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a messages in " + textChannel.getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * Delete a message from a channel
	 * 
	 * @param message
	 */
	public void deleteMessage(Mono<Message> message, String reason) {
		deleteMessage(message.block(), reason);
	}

	/**
	 * Delete a message from a channel
	 * 
	 * @param message
	 */
	public void deleteMessage(Message message, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Deleting message with id: " + message.getId() + " from " + message.getChannel().ofType(TextChannel.class).block().getName());
			message.delete(reason).doOnError(error -> {
				onOutputMessage(MessageType.Error, "Message could not be deleted, error: " + error.getMessage());
				onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + message.getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
			}).block();
			this.getReactionMessageManager().removeEmbeddedMessage(message);
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Message could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + message.getChannel().ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * Delete a message from a channel
	 * 
	 * @param channel
	 * @param messageID
	 */
	public void deleteMessageFromChannel(Mono<Channel> channel, Snowflake messageID, String reason) {
		deleteMessageFromChannel(channel, client.getMessageById(channel.block().getId(), messageID), reason);
	}

	/**
	 * Delete a message from a channel
	 * 
	 * @param channel
	 * @param messageID
	 */
	public void deleteMessageFromMessageChannel(Mono<MessageChannel> channel, Snowflake messageID, String reason) {
		deleteMessageFromChannel(channel.ofType(Channel.class), client.getMessageById(channel.block().getId(), messageID), reason);
	}
	
	
	/**
	 * Delete a message from a channel
	 * 
	 * @param channel
	 * @param messageID
	 */
	public void deleteMessageFromTextChannel(Mono<TextChannel> channel, Snowflake messageID, String reason) {
		deleteMessageFromChannel(channel.ofType(Channel.class), client.getMessageById(channel.block().getId(), messageID), reason);
	}
	

	/**
	 * Delete a message from a channel
	 * 
	 * @param channel
	 * @param message
	 */
	public void deleteMessageFromChannel(Mono<Channel> channel, Mono<Message> message, String reason) {
		if (client == null)
			return;
		if (client.isConnected() == false)
			return;
		try {
			onOutputMessage(MessageType.Info, "Deleting message with id: " + message.block().getId() + " from " + channel.ofType(TextChannel.class).block().getName());
			channel.ofType(TextChannel.class).block().getMessageById(message.block().getId()).block().delete(reason).block();
			this.getReactionMessageManager().removeEmbeddedMessage(message.block());
		} catch (Exception e) {
			onOutputMessage(MessageType.Error, "Message could not be deleted, error: " + e.getMessage());
			onOutputMessage(MessageType.Error, ":warning: unable to delete a message in " + channel.ofType(TextChannel.class).block().getName() + " due to an error, please check the log for details!");
		}
	}

	/**
	 * Edit a already sent message
	 * 
	 * @param oldMessage
	 * @param content
	 * @return
	 */
	public Message editMessage(Mono<Message> oldMessage, final String content) {
		return editMessage(oldMessage, spec -> spec.setContent(content));
	}

	/**
	 * Edit a already sent message
	 * 
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
			onOutputMessage(MessageType.Error, "Message could not be edited, error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Gets the instance of the bot's DiscordClient object
	 * 
	 * @return
	 */
	public DiscordClient getClient() {
		return client;
	}

	/**
	 * Gets the instance of the bot's Dispatcher object
	 * 
	 * @return
	 */
	public EventDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * Gets the instance of the bot's ReactionMessageManager object
	 * 
	 * @return
	 */
	public ReactionMessageManager getReactionMessageManager() {
		return reactionMessageManager;
	}

	/**
	 * Gets the instance of the bot's ReactionMessageManager object
	 * 
	 * @param reactionMessageManager
	 */
	public void setReactionMessageManager(ReactionMessageManager reactionMessageManager) {
		this.reactionMessageManager = reactionMessageManager;
	}

	/**
	 * Gets called when the internal code creates an output
	 * 
	 * @param type
	 * @param message
	 */
	public abstract void onOutputMessage(MessageType type, String message);

	/**
	 * Should be used to register events using {@link #getDispatcher()}.on() Must be called after creating your bot instance
	 */
	public abstract void setupEvents();
}
