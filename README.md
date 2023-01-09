## What is GoBlock?
GoBlock is a highly customizable blockchain simulator. Thanks to its many customization options, it can be used to perform the desired tests on blockchains in the simulator environment, especially for creating new connection algorithms.

## 1. Prerequisites
- GoBlock is available on any operating system that supports Java JDK 1.8.0 or higher.
- Depending on the IDE/compiling method, the gson library inside of the lib folder might be needed to add as a referenced library before compiling the code.

## 2. Input
Besides the in simulator settings, some inputs for simulator can be done in simulator/src/dist/input directory.
- nodes.json: This file contains information for how nodes will be created, whether it's from [bitnodes.io](https://bitnodes.io/) or fully custom made.
- nodes.txt: This file contains information for how nodes will be paired.

## 3. Output
Every event is recorded as json files and can be found under simulator/src/dist/output directory.
- static.json: It has a record of region list.
- output.json: All events are recorded here.
- blockList.txt: All minted blocks are recorded here.
- graph/*: All neighbors of every single node is recorded here.
