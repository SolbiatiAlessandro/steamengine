# steamengine

A VR browser-based **physics engine** written in [clojurescript](https://clojurescript.org/). 

## Examples

1. **[https://www.youtube.com/watch?v=d7r9XxQVUjM](https://www.youtube.com/watch?v=d7r9XxQVUjM)** 3D diffusion of water in a box (from a Oculus Quest 1 browser) 

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/d7r9XxQVUjM/0.jpg)](https://www.youtube.com/watch?v=d7r9XxQVUjM)

2. **[https://youtu.be/LBo67lY-p7E](https://youtu.be/LBo67lY-p7E)** 2D diffusion of water under static diagonal velocity field in a box (from Laptop browser) 

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/LBo67lY-p7E/0.jpg)](https://www.youtube.com/watch?v=LBo67lY-p7E)

3. **[https://youtu.be/eVWq8RlS6tc](https://youtu.be/eVWq8RlS6tc)** 2d diffusion of water (from Laptop browser) 

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/eVWq8RlS6tc/0.jpg)](https://www.youtube.com/watch?v=eVWq8RlS6tc)


## Overview

Features

- realistic hydrodyanmics simulations from the famous paper [Real-Time Fluid Dynamics for Games, Stem 2003](https://www.semanticscholar.org/paper/Real-Time-Fluid-Dynamics-for-Games-Stam/5127ac7b58e36ffd13ca4437fc123c6a018dc436?p2df)
- runs both on laptop browser and in VR browser
- served in Python, written in ClojureScript

You can find in profiling.md some measurements around speed and efficiency of the simulation.

## Clojure Setup

To get an interactive development environment run:

    npm install
    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).

To run tests (you need to have `node` installed)

    lein doo node test

## Python Setup

With `figwheel` running, set up your venv with `flask` and run 

	python python-server/server.py

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
