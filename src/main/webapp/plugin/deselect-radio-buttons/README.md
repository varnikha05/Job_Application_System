# deselectable-radio-buttons
A jQuery plugin that automatically makes all radio buttons deslectable.

It uses mousedown/click and keydown/keyup to detect interaction with a radio button. If a user clicks or presses the space bar on a checked radio button, it will be unchecked.

A custom event named `done_changing` is fired afterwards, so event handlers can do something after the new state of the radio button is determined.
