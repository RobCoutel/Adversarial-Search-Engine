# Adversarial-Search-Engine
@ authors : COUTELIER Robin and PIRENNE Gilles

This is an unfinished project realized by 2 students of the University of Liège.

It consists in a series of adversarial seach methods to play two players, deterministic games.

The differents approaches are :
 - Minimax algorithm with prunning
 - Budget of exploration of the game tree
The evaluation functions are implemented either with a our knowledge of the game, or by a neural Network.

What works and was tested :
 - The implementations of the game of Chess
 - The implementation of the game of TicTacToe
 - The agents : AgentBudget, AgentMinimax, AgentMinimaxSorted
 - The tournament management system (though it can still be improved)

What seems to work but still needs to be further tests
 - The matrix library
 - The Neural Network propagation

What does not work :
 - The neural network's gradient descent does not seem to learn.
 - The genetic algorithm to teach the neural network is also deficient

What will be added soon :
 - The GUI to play chess. Our current version uses Processing 4 to function, but we are seeking a better solution
 - A Monte Carlo tree search algorithm as a new Agent


How does the project work
 - The project works on a bash terminal with java vresion : openjdk 11.0.9 2020-10-20
 - Every command is inputed in the terminal on the directory /GameEngine
 - To compile the files, we use $ ./make.sh
 - To execute the main functions, at the top the the file, there is the command line we use with the arguments

The main functions are located in :
 - com.chess.Chess.java             to play a game of chess between agents
 - com.ticTacToe.TicTacToe.java     to play a game of TicTacToe between agents
 - com.neuralNetworks.Genetics.java to train the neural networks of agents (currently not great)
