/***
 * $Id: Telnet.java,v 1.1 2002/04/03 01:04:28 brekke Exp $
 *
 * NetComponents Internet Protocol Library
 * Copyright (C) 1997-2002  Daniel F. Savarese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library in the LICENSE file; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 ***/

package com.oroinc.net.telnet;

import java.io.*;
import java.net.*;

/***
 *
 * <p>
 *
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

class Telnet extends com.oroinc.net.SocketClient {
  static final boolean debug = /*true;*/ false;

  static final byte[] _COMMAND_DO = {
    (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO
  };

  static final byte[] _COMMAND_DONT = {
    (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT
  };

  static final byte[] _COMMAND_WILL = {
    (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL
  };

  static final byte[] _COMMAND_WONT = {
    (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT
  };

  static final byte[] _COMMAND_SB = {
    (byte)TelnetCommand.IAC, (byte)TelnetCommand.SB
  };

  static final byte[] _COMMAND_SE = {
    (byte)TelnetCommand.IAC, (byte)TelnetCommand.SE
  };

  static final int _WILL_MASK = 0x01, _DO_MASK = 0x02,
                   _REQUESTED_WILL_MASK = 0x04, _REQUESTED_DO_MASK = 0x08;

  /* public */ static final int DEFAULT_PORT = 23;

  int[] _doResponse, _willResponse, _options;

  /* public */ Telnet() {
    setDefaultPort(DEFAULT_PORT);
    _doResponse   = new int[TelnetOption.MAX_OPTION_VALUE + 1];
    _willResponse = new int[TelnetOption.MAX_OPTION_VALUE + 1];
    _options      = new int[TelnetOption.MAX_OPTION_VALUE + 1];
  }


  boolean _stateIsWill(int option) {
    return ((_options[option] & _WILL_MASK) != 0);
  }

  boolean _stateIsWont(int option) { return !_stateIsWill(option); }

  boolean _stateIsDo(int option) {
    return ((_options[option] & _DO_MASK) != 0);
  }

  boolean _stateIsDont(int option) { return !_stateIsDo(option); }

  boolean _requestedWill(int option) {
    return ((_options[option] & _REQUESTED_WILL_MASK) != 0);
  }

  boolean _requestedWont(int option) { return !_requestedWill(option); }

  boolean _requestedDo(int option) {
    return ((_options[option] & _REQUESTED_DO_MASK) != 0);
  }

  boolean _requestedDont(int option) { return !_requestedDo(option); }

  void _setWill(int option)     { _options[option] |= _WILL_MASK; }
  void _setDo(int option)       { _options[option] |= _DO_MASK; }
  void _setWantWill(int option) { _options[option] |= _REQUESTED_WILL_MASK; }
  void _setWantDo(int option)   { _options[option] |= _REQUESTED_DO_MASK; }

  void _setWont(int option)     { _options[option] &= ~_WILL_MASK; }
  void _setDont(int option)     { _options[option] &= ~_DO_MASK; }
  void _setWantWont(int option) { _options[option] &= ~_REQUESTED_WILL_MASK; }
  void _setWantDont(int option) { _options[option] &= ~_REQUESTED_DO_MASK; }


  void _processDo(int option) throws IOException {
    boolean acceptNewState = false;

    if(_willResponse[option] > 0) {
      --_willResponse[option];
      if(_willResponse[option] > 0 && _stateIsWill(option))
	--_willResponse[option];
    }

    if(_willResponse[option] == 0) {
      if(_requestedWont(option)) {

	switch(option) {

	default: break;
	  
	}


	if(acceptNewState) {
	  _setWantWill(option);
	  _sendWill(option);
	} else {
	  ++_willResponse[option];
	  _sendWont(option);
	}
      } else {
	// Other end has acknowledged option.

	switch(option) {

	default: break;
	  
	}

      }
    }

    _setWill(option);
  }


  void _processDont(int option) throws IOException {
    if(_willResponse[option] > 0) {
      --_willResponse[option];
      if(_willResponse[option] > 0 && _stateIsWont(option))
	--_willResponse[option];
    }

    if(_willResponse[option] == 0 && _requestedWill(option)) {

      switch(option) {

      default: break;
	
      }

      _setWantWont(option);

      if(_stateIsWill(option))
	_sendWont(option);
    }

    _setWont(option);
  }


  void _processWill(int option) throws IOException {
    boolean acceptNewState = false;

    if(_doResponse[option] > 0) {
      --_doResponse[option];
      if(_doResponse[option] > 0 && _stateIsDo(option))
	--_doResponse[option];
    }

    if(_doResponse[option] == 0 && _requestedDont(option)) {

      switch(option) {

      default: break;
	  
      }


      if(acceptNewState) {
	_setWantDo(option);
	_sendDo(option);
      } else {
	++_doResponse[option];
	_sendDont(option);
      }
    }

    _setDo(option);
  }


  void _processWont(int option) throws IOException {
    if(_doResponse[option] > 0) {
      --_doResponse[option];
      if(_doResponse[option] > 0 && _stateIsDont(option))
	--_doResponse[option];
    }

    if(_doResponse[option] == 0 && _requestedDo(option)) {

      switch(option) {

      default: break;
	
      }

      _setWantDont(option);

      if(_stateIsDo(option))
	_sendDont(option);
    }

    _setDont(option);
  }


  protected void _connectAction_() throws IOException {
    super._connectAction_();
    _input_  = new BufferedInputStream(_input_);
    _output_ = new BufferedOutputStream(_output_);
  }


  final synchronized void _sendDo(int option) throws IOException {
    if(debug)
      System.err.println("DO: " + TelnetOption.getOption(option));
    _output_.write(_COMMAND_DO);
    _output_.write(option);
  }

  final synchronized void _requestDo(int option) throws IOException {
    if((_doResponse[option] == 0 && _stateIsDo(option)) ||
       _requestedDo(option))
      return;
    _setWantDo(option);
    ++_doResponse[option];
    _sendDo(option);
  }

  final synchronized void _sendDont(int option) throws IOException {
    if(debug)
      System.err.println("DONT: " + TelnetOption.getOption(option));
    _output_.write(_COMMAND_DONT);
    _output_.write(option);
  }

  final synchronized void _requestDont(int option) throws IOException {
    if((_doResponse[option] == 0 && _stateIsDont(option)) ||
       _requestedDont(option))
      return;
    _setWantDont(option);
    ++_doResponse[option];
    _sendDont(option);
  }


  final synchronized void _sendWill(int option) throws IOException {
    if(debug)
      System.err.println("WILL: " + TelnetOption.getOption(option));
    _output_.write(_COMMAND_WILL);
    _output_.write(option);
  }

  final synchronized void _requestWill(int option) throws IOException {
    if((_willResponse[option] == 0 && _stateIsWill(option)) ||
       _requestedWill(option))
      return;
    _setWantWill(option);
    ++_doResponse[option];
    _sendWill(option);
  }

  final synchronized void _sendWont(int option) throws IOException {
    if(debug)
      System.err.println("WONT: " + TelnetOption.getOption(option));
    _output_.write(_COMMAND_WONT);
    _output_.write(option);
  }

  final synchronized void _requestWont(int option) throws IOException {
    if((_willResponse[option] == 0 && _stateIsWont(option)) ||
       _requestedWont(option))
      return;
    _setWantWont(option);
    ++_doResponse[option];
    _sendWont(option);
  }

  final synchronized void _sendByte(int b) throws IOException {
    _output_.write(b);
  }
}
