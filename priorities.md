1.0 release checklist:

 * Implement house rules
 	* Implement the rules themselves
 	* Implement a composite ruleset comprised of individual rules (done)
 	* Synchronize rulesets over the network (done)
 	* Allow configuring the ruleset through the UI (done)
 	* House rules to implement: (done)
 		* Jump In (done)
 			* Will require bespoke code
 			* Will require new kind of action
 			* Will require overriding turns (hrrrg)
 		* 7-0 (done)
 			* Will require overriding cards (done)
 			* Will require new kind of action (done)
 		* Stacking (done)
 			* Will require overriding cards
 			* Will require new kind of action
 		* Draw To Match (done)
 		* Force Play (done)
 		* No Bluffing... whatever that means (done)
 * Ensure synchronization between the network game state representation
   and the internal representation. (done)
 * Implement wild card challenges (done)
 	* Add new action(s) for wild draw 4 cards (done)
 	* Implement the UI for challenging wild draw 4s (done)
 	* Fully implement the action for the wild draw 4 (done)
 * Implement custom decks (done)
 	* Expand this to include custom themes. (done)
 * Fix applet loading screen (N/A)
 	* With the new image caching system, loading times are now so short that
 	  the full loading screen is unnecessary; ie: there's nothing left to fix.
 * Server-side turn validation
 	* Rewrite the network protocol so that the clients send messages describing
      what the player did, not what the result of the player's action was.
 		* The server will still send messages describing the outcome of an
 		  action to other clients.
 	* Rewrite the Game and Card classes to be interfaces so that CardTypes can
 	  operate on both heavyweight UI versions of cards/games and lightweight
 	  network-friendly versions of cards/games to facilitate the server
 	  checking the results.
 	* To avoid visible network lag, clients will still perform actions
 	  independent of the server, with the server merely checking their work and
 	  sending corrections if need be.
 		* In particular, clients will still decide what cards are drawn;
 		  however, it will be done using an RNG whose state is replicated on
 		  the server (same seed, same algorithm) so that the server can
 		  guarantee fairness.
 		  If someone is cheating by modifying the RNG or the card weights, the
 		  server will know.
 * Server daemon
 	* Basically the server should be able to run in the background as a
 	  service, if desired.
 	  * The solution is probably have a flag to have the server run in an
 	    infinite loop on a daemon thread.
 * Full code audit
 	* Add thorough documentation
 	* Remove unused code
 	* Clean up code to be more readable
 	* Take any optimizations I may find.

Post 1.0 release checklist:
 * Custom written, lightweight serialization methods for actions.
 * Add a rule that makes the Uno button appear at all times,
   but punishes you for pressing it too early.
 * Local AI players
 * Loadable, scriptable decks
 * Cascading themes (like Minecraft)
 	* Basic support for this has been implemented but the UI needs to be worked
 	  on so that users can manage theme loading order in an intuitive way.
 * Loading themes directly from filesystem, rather than just ZIP files.
 * Scriptable rules
 * Testing tools
 * Fuzzing tools
 * Beautify the UI
 * Card played animations
 * Implement Uno Flip
 * Non-infringing branding & artwork.