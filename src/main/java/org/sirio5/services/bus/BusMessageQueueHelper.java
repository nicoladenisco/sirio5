/*
 * Copyright (C) 2023 Nicola De Nisco
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.sirio5.services.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Classe di supporto per la creazione di un pacchetto di QueueInfo.
 * Utile quando si vuole costruire un pacchetto messaggi
 * per sottoporlo al BUS in una unica operazione.
 *
 * @author Nicola De Nisco
 */
public class BusMessageQueueHelper
{
  public final List<MessageBus.QueueInfo> busQueue = new ArrayList<>();

  public void sendMessageAsync(int msgID, Object originator, BusContext context)
  {
    MessageBus.QueueInfo qi = new MessageBus.QueueInfo();
    qi.msgID = msgID;
    qi.originator = originator;
    qi.context = context;

    sendMessageAsync(qi);
  }

  public void sendMessageAsyncDelay(int msgID, int delay, Object originator, BusContext context)
  {
    MessageBus.QueueInfo qi = new MessageBus.QueueInfo();
    qi.msgID = msgID;
    qi.delay = delay;
    qi.originator = originator;
    qi.context = context;

    sendMessageAsync(qi);
  }

  public void sendMessagePackAsync(Collection<MessageBus.QueueInfo> clQueue)
  {
    busQueue.addAll(clQueue);
  }

  public void sendMessageAsync(MessageBus.QueueInfo qi)
  {
    busQueue.add(qi);
  }

  public void sendQueueToBus()
  {
    if(!busQueue.isEmpty())
      BUS.sendMessagePackAsync(busQueue);
  }
}
