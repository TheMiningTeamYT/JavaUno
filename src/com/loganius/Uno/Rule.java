package com.loganius.Uno;

import java.io.Serializable;

/**
 * NOTE: Rule processing takes place on a separate thread to enable delaying!
 * If you aren't using Rule.drawToHand (which uses Swing.invokeLater), PLEASE
 * ensure you are manually using Swing.invokeLater to maintain thread safety!
 */
abstract class Rule implements Serializable {
	private static final long serialVersionUID = 1L;

	/*
	 * Default (if all rules return allow) is to deem a move illegal and prevent it.
	 * Precedence order:
	 * ILLEGAL > LEGAL > ALLOW
	 */
	/* Rule did not decide definitively whether move is legal or not */
	static final int ALLOW = 0;
	
	/* Move IS legal */
	static final int LEGAL = 1;
	static final int PREVENT_FALLBACK = 1;
	
	/* Move is NOT legal */
	static final int ILLEGAL = 2;
	static final int STOP = 2;
	
	/* Intended to allow rules to override card types if desired */
	void bind(Game game) {};
	
	int isLegal(Card card, Game game) {return ALLOW;};
	
	/* 
	 * Return true to indicate the draw was handled and prevent further handling.
	 * Note: if you return true, know that NO OTHER ONDRAW HANDLERS WILL BE CALLED
	 * thus the rule must implement ALL desired behavior!
	 * Care should be taken when ordering the rules for a composite ruleset
	 * to achieve the desired behavior.
	 * Rules are encouraged to use Game.drew() to allow running other handlers
	 */
	int onDraw(Game game, RuleSet.Composite ruleset) {return ALLOW;};
}
