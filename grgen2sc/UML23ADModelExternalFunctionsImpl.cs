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
        static GRGEN_MODEL.UML23ADGraph graph;

        public static void setGraph(GRGEN_MODEL.UML23ADGraph graph_)
        {
            graph = graph_;
        }

        ////////////////////////////////////////////////////////////////////

		public static string foo(string existing, string newStr)
        {
            return existing+newStr;
        }

	}
}
