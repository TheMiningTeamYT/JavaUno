1.0 release checklist:

 * Implement house rules
 	* Implement the rules themselves
 	* Implement a composite ruleset comprised of individual rules
 	* Synchronize rulesets over the network
 	* Allow configuring the ruleset through the UI
 	* House rules to implement:
 		* Jump In
 			* Will require bespoke code
 			* Will require new kind of action
 		* 7-0
 			* Will require overriding cards
 			* Will require new kind of action
 		* Stacking (WIP)
 			* Will require overriding cards
 			* Will require new kind of action
 		* Draw To Match (done)
 		* Force Play (done)
 		* No Bluffing... whatever that means
 * Implement wild card challenges
 	* Add new action(s) for wild draw 4 cards
 	* Implement the UI for challenging wild draw 4s
 	* Fully implement the action for the wild draw 4
 * Implement custom decks
 * Fix applet loading screen
 * Server-side turn validation
 	* Reimplement games & cards as interfaces to simulate multiple inheritance.
 * Server daemon
 * Full code audit

Post 1.0 release checklist:
 * Local AI players
 * Loadable, scriptable decks
 * Scriptable rules
 * Testing tools
 * Fuzzing tools
 * Beautify the UI
 * Card played animations
 * Implement Uno Flip
 * Non-infringing branding & artwork.