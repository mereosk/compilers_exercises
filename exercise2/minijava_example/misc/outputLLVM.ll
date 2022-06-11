@.Example_vtable = global [0 x i8*] []
@.A_vtable = global [2 x i8*] []
@.B_vtable = global [4 x i8*] []
define i32 @main() {

	ret i32 0
}
	%-1 = alloca i32
	store i32 0, i32* %-1
