# Discord-Framework
A Discord-Framework made in Java on top of Discord 4J. Made for making the creation of Bot's for Discord quicker

### Basic, How to use/Example of usage
#### MainClass
```
public class MainClass {
	public static DiscordBot bot;
	
	public static void main(String[] args) {
		bot = new DiscordBot("TOKENHERE");
	}
}
```
#### DiscordBot Class
```
public class DiscordBot extends DiscordBotBase {
	public DiscordBot(String token) {
		super(token);
		setupEvents();
	}

	@Override
	public void onOutputMessage(MessageType type, String message) {
		if (type == MessageType.Error)
			this.sendMessage(DiscordBotUtilities.getChannelByID(this.getClient(), Snowflake.of("CHANNEL_ID_HERE")), "Error: " + message)

		else
			System.out.println(message);
	}

	@Override
	public void setupEvents() {
		this.getDispatcher().on(MessageCreateEvent.class).onErrorContinue((t, o) -> this.onOutputMessage(MessageType.Error, "Error while processing ReactionRemoveEvent Error: " + t.getMessage())).subscribe(event -> event.getMessage().getContent().ifPresent(c -> System.out.println("Message was created here is the content" + event.getMessage().getContent().get())));
	}
}

```


#### Current Version: 1.1.6
### With Maven
In your `pom.xml` add:
```xml
<repositories>
  <repository>
    <id>maven.mjrlegends.com</id>
    <url>https://maven.mjrlegends.com/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.mjr.discordframework</groupId>
    <artifactId>Discord-Framework</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```
### With Gradle
In your `build.gradle` add: 
```groovy
repositories {
  	maven {
	    name 'MJRLegends'
	    url = "https://maven.mjrlegends.com/"
    }
}

dependencies {
  compile "com.mjr.discordframework:Discord-Framework:@VERSION@"
}
```

<a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License</a>. **For more information on the license see** https://tldrlegal.com/license/creative-commons-attribution-noncommercial-noderivs-(cc-nc-nd)#summary
