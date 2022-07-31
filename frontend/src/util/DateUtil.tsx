export function printOffsetString(date: Date): string {
    const offsetInSeconds = date.getTimezoneOffset();

    return date.toLocaleString( 'sv', { timeZoneName: 'short' } );
}
