/*
 * Copyright (C) 2020 Nicola De Nisco
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
package org.sirio5.modules.screens;

import org.apache.turbine.TurbineConstants;
import static org.apache.turbine.modules.screens.TemplateScreen.setTemplate;
import org.apache.turbine.modules.screens.VelocityErrorScreen;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;

/**
 * Versione più sicura di VelocityErrorScreen.
 * Previene la possibilità di code injection javascript.
 *
 * @author Nicola De Nisco
 */
public class SecureVelocityErrorScreen extends VelocityErrorScreen
{
  @Override
  protected void doBuildTemplate(PipelineData pipelineData, Context context)
     throws Exception
  {
    RunData data = getRunData(pipelineData);
    String pex = data.getStackTraceException().toString();
    String stk = data.getStackTrace();

    context.put(TurbineConstants.PROCESSING_EXCEPTION_PLACEHOLDER, protectHtmlValues(pex));
    context.put(TurbineConstants.STACK_TRACE_PLACEHOLDER, protectHtmlValues(stk));

    String errorTemplate = TR.getString(
       TurbineConstants.TEMPLATE_ERROR_KEY,
       TurbineConstants.TEMPLATE_ERROR_VM);

    setTemplate(pipelineData, errorTemplate);
  }

  private String protectHtmlValues(String s)
  {
    return SU.purgeForHtml(s);
  }

  public RunData getRunData(PipelineData data)
  {
    if(data instanceof RunData)
      return (RunData) data;

    throw new RuntimeException("Invalid object data.");
  }
}
