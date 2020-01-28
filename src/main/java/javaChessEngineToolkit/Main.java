/*
 * MIT License
 *
 * Copyright (c) 2020 John Nahlen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package javaChessEngineToolkit;

import chesspresso.game.Game;
import chesspresso.move.IllegalMoveException;

import javaChessEngineToolkit.engines.UciEngine;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IllegalMoveException, ExecutionException, InterruptedException, IOException {

		// Replace 'name', 'uciEnginePath', 'estimatedElo' fields
		final Engine whiteEngine = new UciEngine("Yace","yace.exe",2300);
		final Engine blackEngine = new UciEngine("Yace","yace.exe",2300);
		// You can easily swap out an engine. Not all potential engines will be an instance of UciEngine.
		// final Engine blackEngine = new SlightlyStrongerMoveGenerator();

		final String engineHash = "64"; // In Megabytes (MB)

		UciEngine uciWhiteEngine = null;
		if (whiteEngine instanceof UciEngine) {
			uciWhiteEngine = (UciEngine) whiteEngine;
			uciWhiteEngine.setOption("Hash", engineHash);
			uciWhiteEngine.sendIsReady();
		}

		UciEngine uciBlackEngine = null;
		if (blackEngine instanceof UciEngine) {
			uciBlackEngine = (UciEngine) blackEngine;
			uciBlackEngine.setOption("Hash", engineHash);
			uciBlackEngine.sendIsReady();
		}

		// How many games the engines should play.
		final int maxGames = 1;
		int numGames = 0;

		// Setup the engine match parameters.
		final EngineMatchOptions engineMatchOptions = new EngineMatchOptions();
		// Indicate that this should be a timed match.
		// If false, the match will not be timed, even if values are specified for setTime() and setInc().
		engineMatchOptions.setTimed(true);
		engineMatchOptions.setTime(1);
		engineMatchOptions.setInc(0);
		do {
			System.out.println("Match Info");
			System.out.println(String.format("White: %s (%d) Black: %s (%d)",
					whiteEngine.getName(),whiteEngine.getEstimatedElo(),blackEngine.getName(),blackEngine.getEstimatedElo()));

			EngineMatch engineMatch = new EngineMatch(whiteEngine, blackEngine,engineMatchOptions);
			// This is a blocking call, and will not return until the game is over.
			EngineMatchResult engineMatchResult = engineMatch.play();

			numGames++;

			Game game = engineMatchResult.getGame();
			System.out.println(BoardRenderUtils.draw(game.getPosition()));
			ChesspressoUtils.writePgn(game,System.out);

			// You can get an EPD of the last move using the following lines:
			// game.goBack();
			// System.out.println(ChesspressoUtils.getEPD(game,game.getNextMove().getSAN(),null));
		} while (numGames < maxGames);

		if (uciWhiteEngine != null) {
			// Close the process.
			uciWhiteEngine.stop();
		}

		if (uciBlackEngine != null) {
			// Close the process.
			uciBlackEngine.stop();
		}
	}
}
