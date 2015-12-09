# Custom Device Type for Monoprice 11990 Dual Relay Module

The code in this repository will interface with the Monoprice 11990 module in a way that allows *both* switches to report near real-time status back to the SmartThings Hub.  SmartThings ships with a "Z-Wave Device MultiChannel" device type that will (sorta) work, but you'll quickly find that switch 2 doesn't update it's status in the SmartThings app.  This device type will do just that.

The bulk of the work for this was done by Eric Maycock for the Philio PAN04 module.  All I did was remove the parts that didn't work with the Monoprice module and publish it.  Special thanks go to him for all of his work.

## Installation

### Install the Custom Device Type
First, we need to install the custom device type into your SmartThings account.  Don't worry, this won't hurt a bit.

1. Go ahead and install the module, and let SmartThings set it up as a "Z-Wave Device Multichannel".
1. Copy the code from [monoprice-11990.groovy](https://raw.githubusercontent.com/justintime/SmartThings-Monoprice-11990/master/monoprice-11990.groovy) into your clipboard.
1. Log into your developer account at https://graph.api.smartthings.com/  You can sign up for a new account if you don't already have one.  This site is referred to as "the IDE" from here out.
1. Click "My Device Types", then "New Device Type", then click the "From Code" tab.
1. Paste in the code you copied to your clipboard, and click the "Create" button.
1. From the resulting page, click the "Save" button, then click the "Publish" button, and click "For Me" from the dropdown.
1. You've now installed the custom device type, now you need to associate it with your Monoprice relay.

### Associate the Custom Device Type with the Monoprice Relay Module
Once the custom device type is published in your own library, we need to actually associate it with your Monoprice device.  We'll be doing all of this work in the IDE as well.

1. Click "My Devices".  Click on the name of your Monoprice device.  The device will have "Z-Wave Device MultiChannel" in the "Type" column.
1. Click the "Edit" button.
1. On the resulting page, click the "Type" dropdown.  Scroll all the way to the bottom, and select "Monoprice 11990 Dual Relay Module".
1. Click the "Update" button.

Congratulations, you're done setting up the device!  Go ahead and test it out by toggling the switches from the SmartThings app as well as from the switches themselves.  Make sure everything's working as planned before moving onto the SmartApp installation.

### Install the dual relay adapter SmartApp

SmartThings doesn't really allow you to interact with the individual switches on a multichannel device very well.  So, we'll be creating two "virtual switches".  These virtual switches will look like a normal on/off switch to the SmartThings app, but in reality will just be associated to one of the two switches on our relay.

 