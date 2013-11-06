using System;
using System.Collections.Generic;
using GRGEN_LIBGR = de.unika.ipd.grGen.libGr;
using GRGEN_LGSP = de.unika.ipd.grGen.lgsp;

namespace de.unika.ipd.grGen.Model_UML23AD
{/*
    public partial class Own
    {
        public bool muh()
        {
            return false;
        }
    }

    public partial class OwnPown : Own
    {
        public string ehe;
    }*/
}

namespace de.unika.ipd.grGen.expression
{
    using GRGEN_MODEL = de.unika.ipd.grGen.Model_UML23AD;

	public partial class ExternalFunctions
	{
        ////////////////////////////////////////////////////////////////////

		public static string foo(GRGEN_LIBGR.IActionExecutionEnvironment actionEnv, GRGEN_LIBGR.IGraph graph, string existing, string newStr)
        {
            return existing+newStr;
        }

	}
}
