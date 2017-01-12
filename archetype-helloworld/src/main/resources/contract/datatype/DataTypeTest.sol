pragma solidity ^0.4.7;

import "github.com/Arachnid/solidity-stringutils/strings.sol";

//FAQ
//1, string[] are not allowed as fuction arguments(both input and output);

contract DataTypeTest
{

    using strings for *;

    address public owner;
    function DataTypeTest(){
        owner = msg.sender;
    }
    bytes16 public var1;
    string[] public var2;

    struct StructDef {
            bytes16 var1;
            string[] var2;
     }
     mapping(bytes16 => StructDef) public var3;

    function setBytesN(bytes16 _var1)  {
       var1 = _var1;
    }

    function getBytesN() constant returns (bytes16){
       return var1;
    }

    function pushString(string _var2) {
       var2.push(_var2);
    }

    function testUsinglibrary() constant returns (uint256) {
      return "var2".toSlice().len();
    }

    function setVar(bytes16 _var1, string _var2)  returns (bool  result)  {
        var1 = _var1;
        var2.push(_var2);
    }

    function setStruct(bytes16 _var1, string _var2) {
        StructDef memory mem;
        mem.var1=_var1;
        var3[_var1] = mem;
        var3[_var1].var2.push(_var2);
     }

}