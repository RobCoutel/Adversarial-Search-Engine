SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

java --class-path ${SCRIPT_DIR}/bin com/neuralNetworks/Genetics 32 10 1 4 8 0 Chess