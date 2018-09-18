AvaIre Synchronize Data
=======================

AvaIre Synchronize Data is a plugin written for the [AvaIre cutting edge](https://avairebot.com/invite-cutting-edge) bot, the plugin allows users to synchronize the data between the main bot and the cutting edge bot by downloading all the server, xp, and playlist data.

## How to Use

> **Note:** This plugin was not written with the intention of it being used by self-hosters, no support will be given on how to setup or use the plugin outside of the guide that can be found below.

 1. Clone the repository using git.
```
git clone https://github.com/avaire/synchronize-data-plugin.git
cd synchronize-data-plugin
```
 2. Build the jar file using Gradle.
```
gradle build
```
 3. Copy the built jar file into the AvaIre `plugins` folder, the built jar file can be found at `build/libs/AvaIre.jar`.
 4. Start up Ava to make Ava recognize the plugin.
 5. Edit the `config.yml` with the old database that the database should be pulled from.
 6. Reload the configuration using the `;reload` system command, or by restarting the bot.

And you're done!

## License

AvaIre Synchronize Data is open-sourced software licensed under the [MIT License](https://opensource.org/licenses/MIT).
