module sampledefs;
// A sample file with all kinds of DefUnits

import pack.sample : ImportSelectiveAlias = SampleClassB;

import ImportAliasingDefUnit = pack.sample;

static import pack.sample;

alias TargetFoo Alias;

class Class  {
	int fieldA;
	
	/*this*/ this(int param) {}
	/*~this*/ ~this() {}
	
	/*new*/ new() {}
	/*delete*/ delete() {}
	
	void methodB() { }
}

enum Enum { EnumMemberA, EnumMemberB }

interface Interface { }

struct Struct { }

typedef TargetBar Typedef;

union Union { }

int variable;

template Template(
	TypeParam,
	int ValueParam,
	alias AliasParam,
	TupleParam...
) { 
	
	class TplNestedClass  {
		static /*static this*/ this() {}
		static /*static ~this*/ ~this() {}
		
		void func(asdf.qwer parameter) {
			static if(is(T IfTypeDefUnit : Foo)) {
				/+@CC1+/
			}
			
			{
				Enum e;
				e = Enum.E/+@CC2+/;
			}
		}
	}
}
