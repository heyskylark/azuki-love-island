import { useState } from "react";
import { Link } from "react-router-dom";

function Nav() {
    const [mobileMenu, setMobileMenu] = useState<boolean>(false);

    function toggleMobileMenu(): void {
        setMobileMenu(!mobileMenu);
    }

    function turnOffMobileMenu(): void {
        setMobileMenu(false);
    }

    return (
        <>
        <div className="container mx-auto">
            <div className="container fixed w-full top-0 lg:px-8 px-5 lg:pt-8 pt-5 z-50">
                <div className="flex h-full border-b border-white items-center justify-center max-w-11xl mx-auto border-opacity-0">
                    <div className="flex-grow">
                        <div className="flex">
                            <Link onClick={turnOffMobileMenu} className="w-min-content" to="/">
                                <img className="h-7 p-2 rounded hover:bg-red-600 bg-azukired" src="/svg/azuki-logo.svg" alt="Azuki Logo" />
                            </Link>
                        </div>
                    </div>

                    <div className="items-center hidden lg:flex">
                        {/* <ul className="flex space-x-2">
                            <Link onClick={turnOffMobileMenu} className="mr-2 bg-opacity-20 text-black items-center relative h-7 items-center font-mono tracking-wider pt-0.5 first::pt-0 duration-1000 uppercase text-xs font-500 padding-huge bg-white duration-200 items-center px-4 hover:bg-opacity-70 rounded flex justify-center flex-row" to="/gallery">Gallery</Link>
                        </ul> */}
                        <ul className="flex space-x-2">
                            <Link onClick={turnOffMobileMenu} className="bg-opacity-20 text-black items-center relative h-7 items-center font-mono tracking-wider pt-0.5 first::pt-0 duration-1000 uppercase text-xs font-500 padding-huge bg-white duration-200 items-center px-4 hover:bg-opacity-70 rounded flex justify-center flex-row" to="/credits">Credits</Link>
                        </ul>
                    </div>

                    <div className="lg:hidden z-50">
                        <div className="single-small">
                            <button onClick={toggleMobileMenu} className="hamburger single-small single-small--magnetic">
                                <div className="inner transition ease-in-out duration-500">
                                    <span className="before:bg-black after:bg-black bar transition ease-in-out duration-500" />
                                    <span className="before:bg-black after:bg-red-400 bg-gray-400 bar" />
                                    <span className="before:bg-black after:bg-black bar transition ease-in-out duration-500" />
                                </div>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div className={`lg:hidden w-screen items-start h-screen flex flex-col fixed px-6 pt-20 bg-white bg-opacity-70 backdrop-blur-lg z-40 pointer will-change-opacity overflow-scroll ${!mobileMenu ? "hidden" : ""}`}>
                <ul className="text-2xl w-full uppercase font-bold">
                    <li className="border-b border-black border-opacity-10">
                        <Link className="py-3 w-full items-center block relative flex text-black" to="/credits" onClick={turnOffMobileMenu}>
                            Credits
                        </Link>
                    </li>
                </ul>
            </div>
        </div>
        </>
    );
}

export default Nav;
