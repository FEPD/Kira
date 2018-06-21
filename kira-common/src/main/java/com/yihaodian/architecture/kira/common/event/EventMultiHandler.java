/*
 *  Copyright 2018 jd.com
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yihaodian.architecture.kira.common.event;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventMultiHandler implements EventHandler<Event> {

  private static Logger logger = LoggerFactory.getLogger(EventMultiHandler.class);

  private List<EventHandler<Event>> eventHandlerList;

  public EventMultiHandler() {
    eventHandlerList = new ArrayList<EventHandler<Event>>();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handle(Event event) {
    for (EventHandler<Event> handler : eventHandlerList) {
      try {
        handler.handle(event);
      } catch (Exception e) {
        logger.error(
            "Error occurs when handle event by handler in EventMultiHandler. handler={} and event={}",
            handler, event);
      }
    }
  }

  public void addEventHandler(EventHandler<Event> eventHandler) {
    eventHandlerList.add(eventHandler);
  }

}
