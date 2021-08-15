SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

javac -d ${SCRIPT_DIR}/bin ${SCRIPT_DIR}/com/agents/*.java;
javac -d ${SCRIPT_DIR}/bin ${SCRIPT_DIR}/com/chess/*.java;
javac -d ${SCRIPT_DIR}/bin ${SCRIPT_DIR}/com/gameEngine/*.java;
javac -d ${SCRIPT_DIR}/bin ${SCRIPT_DIR}/com/matrix/*.java;
javac -d ${SCRIPT_DIR}/bin ${SCRIPT_DIR}/com/neuralNetworks/*.java;
javac -d ${SCRIPT_DIR}/bin ${SCRIPT_DIR}/com/ticTacToe/*.java;
javac -d ${SCRIPT_DIR}/bin ${SCRIPT_DIR}/com/tournament/*.java;
javac -d ${SCRIPT_DIR}/bin ${SCRIPT_DIR}/DigitRecognition.java;
