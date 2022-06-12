function Nav() {
    return (
        <div className="fixed w-full top-0 lg:px-8 px-5 lg:pt-8 pt-5 z-70">
            <div className="flex h-full border-b border-white items-center justify-center max-w-11xl mx-auto border-opacity-0">
                <div className="flex-grow">
                    <div className="flex">
                        <a className="w-min-content" href="/">
                            <img className="h-7 p-2 rounded hover:bg-red-600 bg-azukired" src="/svg/azuki-logo.svg" alt="Azuki Logo" />
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Nav;
