/**
 * Abstract class for user
 */
class User {
    // First name
	public firstName: string;
	// Last name
	public lastName: string;
	
	getFullName(): string {
		return this.firstName + this.lastName;
	}
}